package com.coursemanagerfx.logic.security;

import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.InflaterInputStream;

import static com.coursemanagerfx.logic.security.CmanSecuritySaver.*;

public class CmanSecurityParser {
    public static boolean tryParse(File file, String password) {
        try {
            parse(file, password);  // если всё ок — true
            return true;
        } catch (javax.crypto.AEADBadTagException e) {
            System.out.println("Неверный пароль (контрольная метка не совпала)");
            return false;
        } catch (SecurityException e) {
            System.out.println("Неверный пароль (хэш не совпал)");
            return false;
        } catch (Exception e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
            return false;
        }
    }

    /**
     * Расшифровывает, проверяет хэш и парсит группы из файла .cman
     */
    public static Group[] parse(File file, String password) throws Exception {
        try {
            // 1) читаем зашифрованный файл
            byte[] encrypted = Files.readAllBytes(file.toPath());

            // 2) расшифровываем + распаковываем
            SecretKey key = CmanSecurity.deriveKey(password, UNIQUE_SALT);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, UNIQUE_IV));

            ByteArrayOutputStream plainBaos = new ByteArrayOutputStream();
            try (CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(encrypted), cipher);
                 InflaterInputStream infl = new InflaterInputStream(cis)) {
                byte[] buf = new byte[4096];
                int len;
                while ((len = infl.read(buf)) > 0) {
                    plainBaos.write(buf, 0, len);
                }
            }
            byte[] plain = plainBaos.toByteArray();

            // 3) находим MAGIC в конце и отделяем тело от футера
            byte[] magicBytes = MAGIC.getBytes(StandardCharsets.US_ASCII);
            int footerPos = indexOf(plain, magicBytes);
            if (footerPos < 0) throw new IOException("Invalid format: MAGIC not found");

            byte[] body = Arrays.copyOfRange(plain, 0, footerPos);
            DataInputStream metaIn = new DataInputStream(
                    new ByteArrayInputStream(plain, footerPos + magicBytes.length, plain.length - footerPos - magicBytes.length)
            );
            int hlen = metaIn.readInt();
            byte[] fileHash = new byte[hlen];
            metaIn.readFully(fileHash);

            // 4) проверяем хэш
            byte[] expected = CmanSecurity.veracryptStyleHash(password, UNIQUE_SALT);
            if (!Arrays.equals(fileHash, expected)) {
                throw new SecurityException("Invalid password or file corrupted");
            }

            // 5) парсим тело в Group[]
            return parsePlain(body);

        } catch (GeneralSecurityException e) {
            throw new IOException("Decryption error", e);
        }
    }

    // ----- разбор «тела» (.cman без шифра и футера) -----

    private static Group[] parsePlain(byte[] buf) throws IOException {
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(buf));
        byte[] magic = new byte[4];
        dis.readFully(magic);
        if (!"HEAD".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("Wrong header magic");
        }
        int groupsCount = dis.readInt();
        int offSTDS = dis.readInt();
        int offTBLN = dis.readInt();
        int offEVTS = dis.readInt();
        int offTBLE = dis.readInt();
        dis.skipBytes(8);

        // STDS
        DataInputStream stdsIn = new DataInputStream(
                new ByteArrayInputStream(buf, offSTDS, buf.length - offSTDS)
        );
        stdsIn.readFully(magic);
        if (!"STDS".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("Expected STDS section");
        }
        List<List<HolderStudent>> rawGroups = new ArrayList<>();
        for (int i = 0; i < groupsCount; i++) {
            int cnt = stdsIn.readUnsignedShort();
            List<HolderStudent> list = new ArrayList<>();
            for (int j = 0; j < cnt; j++) {
                int sid = stdsIn.readInt(), nameOff = stdsIn.readInt();
                list.add(new HolderStudent(sid, nameOff));
            }
            rawGroups.add(list);
        }

        // TBLN (имена)
        int namesLen = offEVTS - (offTBLN + 4);
        byte[] namesBuf = new byte[namesLen];
        System.arraycopy(buf, offTBLN + 4, namesBuf, 0, namesLen);

        List<List<Student>> groups = new ArrayList<>();
        for (List<HolderStudent> raw : rawGroups) {
            List<Student> studs = new ArrayList<>();
            for (HolderStudent h : raw) {
                studs.add(new Student(readString(namesBuf, h.nameOffset), h.id));
            }
            groups.add(studs);
        }

        // EVTS
        DataInputStream evtsIn = new DataInputStream(
                new ByteArrayInputStream(buf, offEVTS, buf.length - offEVTS)
        );
        evtsIn.readFully(magic);
        if (!"EVTS".equals(new String(magic, StandardCharsets.US_ASCII))) {
            throw new IOException("Expected EVTS section");
        }
        int totalE = evtsIn.readInt();
        List<HolderEvent> rawE = new ArrayList<>();
        for (int i = 0; i < totalE; i++) {
            rawE.add(new HolderEvent(
                    evtsIn.readInt(),
                    evtsIn.readInt(),
                    evtsIn.readInt(),
                    evtsIn.readInt(),
                    evtsIn.readInt()
            ));
        }

        // TBLE (тексты)
        int textLen = buf.length - (offTBLE + 4);
        byte[] textBuf = new byte[textLen];
        System.arraycopy(buf, offTBLE + 4, textBuf, 0, textLen);

        // распределяем события
        for (HolderEvent e : rawE) {
            String cd = readString(textBuf, e.creationOffset);
            int idx = e.creationOffset;
            while (idx < textBuf.length && textBuf[idx] != 0) idx++;
            String desc = idx + 1 < textBuf.length ? readString(textBuf, idx + 1) : "";
            String ed = readString(textBuf, e.expiredOffset);
            StudentEvent se = new StudentEvent(
                    e.eventID, parseEventDate(cd), desc, e.mark, parseEventDate(ed)
            );
            for (List<Student> grp : groups) {
                for (Student s : grp) {
                    if (s.getID() == e.studentId) {
                        s.getEvents().add(se);
                        break;
                    }
                }
            }
        }

        Group[] result = new Group[groupsCount];
        for (int i = 0; i < groupsCount; i++) {
            Group g = new Group();
            g.setStudents(groups.get(i));
            result[i] = g;
        }
        return result;
    }

    private static int indexOf(byte[] array, byte[] target) {
        outer:
        for (int i = 0; i < array.length - target.length + 1; i++) {
            for (int j = 0; j < target.length; j++) {
                if (array[i + j] != target[j]) continue outer;
            }
            return i;
        }
        return -1;
    }

    private static String readString(byte[] buf, int off) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = off;
        while (i < buf.length && buf[i] != 0) {
            baos.write(buf[i] == (byte)0xFF ? ' ' : buf[i]);
            i++;
        }
        return baos.toString(StandardCharsets.UTF_8);
    }

    private static EventDate parseEventDate(String s) {
        String[] p = s.split("\\.");
        return new EventDate(
                Integer.parseInt(p[0]),
                Integer.parseInt(p[1]),
                Integer.parseInt(p[2])
        );
    }

    private static class HolderStudent {
        final int id, nameOffset;
        HolderStudent(int id, int nameOffset) {
            this.id = id; this.nameOffset = nameOffset;
        }
    }
    private static class HolderEvent {
        final int eventID, studentId, creationOffset, mark, expiredOffset;
        HolderEvent(int eid, int sid, int co, int mark, int eo) {
            this.eventID = eid;
            this.studentId = sid;
            this.creationOffset = co;
            this.mark = mark;
            this.expiredOffset = eo;
        }
    }

    public static void main(String[] args) {
        File file = new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\TestSecurity.cman");
        try {
            String password = "123456789#";
            if (tryParse(file, password)) {
                Group[] groups = parse(file, password);
                for (int i = 0; i < groups.length; i++) {
                    System.out.println("Группа " + (i + 1) + ": " + groups[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
