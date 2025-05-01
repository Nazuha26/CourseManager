package com.coursemanagerfx.logic.security;

import com.coursemanagerfx.logic.basic.Course;
import com.coursemanagerfx.logic.basic.Group;
import com.coursemanagerfx.logic.basic.Student;
import com.coursemanagerfx.logic.basic.event.EventMods;
import com.coursemanagerfx.logic.basic.event.StudentEvent;
import com.coursemanagerfx.logic.basic.event.date.EventDate;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/*
 * Формат сырого сериализованного бинарного .cman-файла:
 *
 * ┌──────────────┬─────────────────────────────────────────────────────────────────────┐
 * │ Секция       │ Описание                                                            │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ HEADER       │ • "HEAD" (4 байта, ASCII)                                           │
 * │ (32 байта)   │ • groupsCount (4 байта, int)                                        │
 * │              │ • offsetSTDS (4 байта, uint32)                                      │
 * │              │ • offsetTBLN (4 байта, uint32)                                      │
 * │              │ • offsetEVTS (4 байта, uint32)                                      │
 * │              │ • offsetTBLE (4 байта, uint32)                                      │
 * │              │ • резерв (8 байт нулей)                                             │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ STDS         │ • "STDS" (4 байта, ASCII)                                           │
 * │              │ Для каждой из groupsCount групп:                                    │
 * │              │   • studentCount (2 байта, short)                                   │
 * │              │   Для каждого студента:                                             │
 * │              │     – studentID (4 байта, int)                                      │
 * │              │     – nameOffset (4 байта, uint32, смещение в TBLN)                 │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ TBLN         │ • "TBLN" (4 байта, ASCII)                                           │
 * │ (таблица имён)│ • Нуль-терминированные строки:                                     │
 * │              │   – UTF-8 байты имени                                               │
 * │              │   – пробел заменяется байтом 0xFF                                   │
 * │              │   – завершение строки: 0x00                                         │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ EVTS         │ • "EVTS" (4 байта, ASCII)                                           │
 * │              │ • totalEvents (4 байта, int)                                        │
 * │              │ Для каждого события:                                                │
 * │              │   – eventID (4 байта, int)                                          │
 * │              │   – studentID (4 байта, int)                                        │
 * │              │   – creationOffset (4 байта, uint32, смещение в TBLE)               │
 * │              │   – mark (4 байта, int)                                             │
 * │              │   – expiredOffset (4 байта, uint32, смещение в TBLE)                │
 * ├──────────────┼─────────────────────────────────────────────────────────────────────┤
 * │ TBLE         │ • "TBLE" (4 байта, ASCII)                                           │
 * │ (тексты)     │ • Нуль-терминированные строки:                                      │
 * │              │   – UTF-8 байты (дата создания, описание, дата истечения)           │
 * │              │   – пробел заменяется байтом 0xFF                                   │
 * │              │   – завершение каждой строки: 0x00                                  │
 * └──────────────┴─────────────────────────────────────────────────────────────────────┘
 */

// ======= Пояснение: безопасное сохранение .cman =======
//
//  1) Из пароля и константной соли генерируются:
//     - секретный ключ для AES (PBKDF2WithHmacSHA512),
//     - хэш пароля в стиле VeraCrypt для последующей проверки (VeraCrypt SHA512 + XTS 1024 bit);
//  2) Инициализируется шифр AES/GCM/NoPadding с фиксированным IV (Initialization Vector)
//     для обеспечения целостности и аутентичности;
//  3) В единую последовательность записываются:
//     - сериализованные данные курса (тело),
//     - мета-блок (MAGIC + хэш);
//  4) Полученный блок сжимается через DeflaterOutputStream (алгоритм Deflate);
//  5) Сжатые данные шифруются через CipherOutputStream (AES-GCM);
//  6) Итоговый зашифрованно-сжатый блок целиком записывается в выходной файл.
//
// =========================================================

public class CmanSecuritySaver {
    public static final String MAGIC = "CMS1";             // файл-магия
    public static final byte[] UNIQUE_SALT = new byte[] {
            (byte)0xA7, (byte)0x1C, (byte)0x3D, (byte)0xB2,
            (byte)0x56, (byte)0x9E, (byte)0x10, (byte)0x4F,
            (byte)0x81, (byte)0x3A, (byte)0xFA, (byte)0x2B,
            (byte)0x6D, (byte)0x79, (byte)0x0C, (byte)0xE3
    };

    public static final byte[] UNIQUE_IV = new byte[] {
            (byte)0x5A, (byte)0x4E, (byte)0x92, (byte)0x31,
            (byte)0xBC, (byte)0xD7, (byte)0x08, (byte)0xC4,
            (byte)0x1F, (byte)0xA3, (byte)0x6E, (byte)0x99
    };

    /** Сохраняет зашифрованный .cman в `outFile` и возвращает пароль. */
    /*public static boolean save(Course course, File outFile) throws Exception {
        String password = course.getPassword();
        byte[] salt = course.getSalt();
        byte[] iv = course.getIv();
        Group[] groups = course.getGroups();

        SecretKey key = CmanSecurity.deriveKey(password, UNIQUE_SALT);
        byte[] hash = CmanSecurity.veracryptStyleHash(password, salt);  // правильный хэш

        // 1) Сериализуем всё в plain-буфер (включая метаинфо)
        ByteArrayOutputStream plainBaos = new ByteArrayOutputStream();
        try (DataOutputStream plainOut = new DataOutputStream(plainBaos)) {
            // --- сначала meta-блок ---
            plainOut.writeInt(hash.length);  // длина хэша
            plainOut.write(hash);            // сам хэш

            plainOut.writeInt(salt.length);
            plainOut.write(salt);

            plainOut.writeInt(iv.length);
            plainOut.write(iv);

            // --- теперь обычные данные курса ---
            BinaryCmanSaverReplica.writePlain(groups, plainOut);
        }

        // 2) Пишем в файл: MAGIC + зашифрованный блок
        try (FileOutputStream fos = new FileOutputStream(outFile)) {
            fos.write(MAGIC.getBytes(StandardCharsets.US_ASCII)); // 4 байта сигнатуры

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            try (DeflaterOutputStream def =
                         new DeflaterOutputStream(new CipherOutputStream(fos, cipher))) {
                def.write(plainBaos.toByteArray()); // всё — мета + данные
            }
        }

        return true;
    }*/
    public static boolean save(Group[] groups, File outFile, String password) throws Exception {
        //Group[] groups = course.getGroups();

        // 1. сериализуем "тело" — все данные курса
        ByteArrayOutputStream bodyBaos = new ByteArrayOutputStream();
        try (DataOutputStream bodyOut = new DataOutputStream(bodyBaos)) {
            BinaryCmanSaverReplica.writePlain(groups, bodyOut);
        }

        // 2. готовим метаинфо
        byte[] hash = CmanSecurity.veracryptStyleHash(password, UNIQUE_SALT);
        ByteArrayOutputStream footerBaos = new ByteArrayOutputStream();
        try (DataOutputStream metaOut = new DataOutputStream(footerBaos)) {
            metaOut.write(MAGIC.getBytes(StandardCharsets.US_ASCII)); // 4 байта

            metaOut.writeInt(hash.length);
            metaOut.write(hash);
        }

        // 3. объединяем тело и футер
        ByteArrayOutputStream fullPlain = new ByteArrayOutputStream();
        fullPlain.write(bodyBaos.toByteArray());     // тело
        fullPlain.write(footerBaos.toByteArray());   // мета (CMS1 + хэш, соль, IV)

        // 4. шифруем всё одним блоком
        SecretKey key = CmanSecurity.deriveKey(password, UNIQUE_SALT);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, UNIQUE_IV));

        try (FileOutputStream fos = new FileOutputStream(outFile);
             CipherOutputStream cos = new CipherOutputStream(fos, cipher);
             DeflaterOutputStream deflater = new DeflaterOutputStream(cos)) {

            deflater.write(fullPlain.toByteArray()); // всё — тело + footer
        }

        return true;
    }

    public static void decryptToFile(File encryptedFile, File outputFile, String password) throws Exception {
        // Ключ и IV у тебя фиксированы
        SecretKey key = CmanSecurity.deriveKey(password, UNIQUE_SALT);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, UNIQUE_IV));

        try (FileInputStream fis = new FileInputStream(encryptedFile);
             CipherInputStream cis = new CipherInputStream(fis, cipher);
             InflaterInputStream infl = new InflaterInputStream(cis);
             FileOutputStream out = new FileOutputStream(outputFile)) {

            byte[] buffer = new byte[4096];
            int r;
            while ((r = infl.read(buffer)) > 0) {
                out.write(buffer, 0, r);
            }
        }
    }


    private static class SaverEvent {
        int id, studentId, mark;
        String creationDate, description, expiredDate;

        SaverEvent(StudentEvent e, int studentId) {
            this.id = e.getID();
            this.studentId = studentId;
            this.creationDate = e.getCrtDate().toString();      // EventDate → String
            this.description = e.getDescription();
            this.mark = e.getMark();
            this.expiredDate = e.getExpDate().toString();        // EventDate → String
        }
    }

    // Внутренний класс: дословно повторяет BinaryCmanSaver.save в память
    private static class BinaryCmanSaverReplica {
        /** Пишет плейнформат .cman в out */
        static void writePlain(Group[] groups, DataOutput out) throws IOException {
            // 1) Собираем таблицу имён
            ByteArrayOutputStream namesBaos = new ByteArrayOutputStream();
            Map<String, Integer> nameOffset = new HashMap<>();
            for (Group g : groups) {
                for (Student s : g.getStudents()) {
                    String name = s.getName();
                    if (!nameOffset.containsKey(name)) {
                        int off = namesBaos.size();
                        nameOffset.put(name, off);
                        // запись UTF-8, пробел -> 0xFF
                        for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
                            namesBaos.write(b == ' ' ? 0xFF : b);
                        }
                        namesBaos.write(0x00);
                    }
                }
            }
            byte[] namesTable = prependMagic("TBLN", namesBaos.toByteArray());

            // 2) Собираем таблицу текстов ивентов
            ByteArrayOutputStream textsBaos = new ByteArrayOutputStream();
            Map<String, Integer> textOffset = new HashMap<>();
            List<CmanSecuritySaver.SaverEvent> allEvents = new ArrayList<>();
            for (Group g : groups) {
                for (Student s : g.getStudents()) {
                    for (StudentEvent e : s.getEvents()) {
                        allEvents.add(new CmanSecuritySaver.SaverEvent(e, s.getID()));
                    }
                }
            }
            for (CmanSecuritySaver.SaverEvent e : allEvents) {
                for (String txt : Arrays.asList(e.creationDate, e.description, e.expiredDate)) {
                    if (!textOffset.containsKey(e.id + "|" + txt)) {
                        int off = textsBaos.size();
                        textOffset.put(e.id + "|" + txt, off);
                        for (byte b : txt.getBytes(StandardCharsets.UTF_8)) {
                            textsBaos.write(b == ' ' ? 0xFF : b);
                        }
                        textsBaos.write(0x00);
                    }
                }
            }
            byte[] textTable = prependMagic("TBLE", textsBaos.toByteArray());

            // 3) Собираем секцию STDS
            ByteArrayOutputStream stdsBaos = new ByteArrayOutputStream();
            stdsBaos.write(ascii("STDS"));
            DataOutputStream dosStds = new DataOutputStream(stdsBaos);
            for (Group g : groups) {
                dosStds.writeShort(g.getStudents().size());
                for (Student s : g.getStudents()) {
                    dosStds.writeInt(s.getID());
                    dosStds.writeInt(nameOffset.get(s.getName()));
                }
            }
            byte[] stdsSection = stdsBaos.toByteArray();

            // 4) Собираем секцию EVTS
            ByteArrayOutputStream evtsBaos = new ByteArrayOutputStream();
            evtsBaos.write(ascii("EVTS"));
            DataOutputStream dosEvts = new DataOutputStream(evtsBaos);
            dosEvts.writeInt(allEvents.size());
            for (CmanSecuritySaver.SaverEvent e : allEvents) {
                dosEvts.writeInt(e.id);
                dosEvts.writeInt(e.studentId);
                dosEvts.writeInt(textOffset.get(e.id + "|" + e.creationDate));
                dosEvts.writeInt(e.mark);
                dosEvts.writeInt(textOffset.get(e.id + "|" + e.expiredDate));
            }
            byte[] evtsSection = evtsBaos.toByteArray();

            // 5) Подсчитываем оффсеты
            int headerSize = 32;
            int offSTDS = headerSize;
            int offTBLN = offSTDS + stdsSection.length;
            int offEVTS = offTBLN + namesTable.length;
            int offTBLE = offEVTS + evtsSection.length;

            // 6) Пишем всё в файл
            // HEADER
            out.write(ascii("HEAD"));
            out.writeInt(groups.length);
            out.writeInt(offSTDS);
            out.writeInt(offTBLN);
            out.writeInt(offEVTS);
            out.writeInt(offTBLE);
            out.writeLong(0L); // 8 байт нулей

            // Секции
            out.write(stdsSection);
            out.write(namesTable);
            out.write(evtsSection);
            out.write(textTable);
        }

        private static byte[] ascii(String s) { return s.getBytes(); }

        private static byte[] prependMagic(String magic, byte[] data) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(magic.getBytes());
            baos.write(data);
            return baos.toByteArray();
        }
    }

    public static void main(String[] args) throws IOException {
        // Группа 1
        List<StudentEvent> events1 = new ArrayList<>();
        events1.add(new StudentEvent(10001, new EventDate(1, 1, 2025), "Math exam", EventMods.FIRST));
        events1.add(new StudentEvent(10002, new EventDate(3, 1, 2025), "Biology lab", 4, new EventDate(8, 1, 2025)));
        events1.add(new StudentEvent(10003, new EventDate(5, 1, 2025), "Literature essay: Lesya Ukrainka", EventMods.SECOND));
        events1.add(new StudentEvent(10004, new EventDate(7, 1, 2025), "Physics forces quiz", 5, new EventDate(14, 1, 2025)));
        events1.add(new StudentEvent(10005, new EventDate(10, 1, 2025), "World History group project", EventMods.THIRD));

        List<StudentEvent> events2 = new ArrayList<>();
        events2.add(new StudentEvent(20001, new EventDate(2, 1, 2025), "Computer graphics assignment", EventMods.FIRST));
        events2.add(new StudentEvent(20002, new EventDate(4, 1, 2025), "Music theory test", 3, new EventDate(9, 1, 2025)));
        events2.add(new StudentEvent(20003, new EventDate(6, 1, 2025), "Technology & Robotics demo", EventMods.SECOND));
        events2.add(new StudentEvent(20004, new EventDate(9, 1, 2025), "Art portfolio presentation", 5, new EventDate(16, 1, 2025)));
        events2.add(new StudentEvent(20005, new EventDate(11, 1, 2025), "Photography contest submission", EventMods.THIRD));

        List<StudentEvent> events3 = new ArrayList<>();
        events3.add(new StudentEvent(30001, new EventDate(1, 1, 2025), "English debate", EventMods.SECOND));
        events3.add(new StudentEvent(30002, new EventDate(3, 1, 2025), "Ukrainian dictation", 4, new EventDate(6, 1, 2025)));
        events3.add(new StudentEvent(30003, new EventDate(5, 1, 2025), "Ethics case discussion", EventMods.FIRST));
        events3.add(new StudentEvent(30004, new EventDate(8, 1, 2025), "Psychology quiz", 5, new EventDate(12, 1, 2025)));
        events3.add(new StudentEvent(30005, new EventDate(10, 1, 2025), "Public speaking workshop", EventMods.THIRD));


        Student student1 = new Student("Артем");
        student1.setEvents(events1);

        Student student2 = new Student("Оля");
        student2.setEvents(events2);

        Group group1 = new Group();
        group1.setStudents(List.of(student1, student2));

        // Группа 2
        Student student3 = new Student("Максим");
        student3.setEvents(events2);

        Group group2 = new Group();
        group2.setStudents(List.of(student3));

        // Группа 3
        Student student4 = new Student("Ірина");
        student4.setEvents(events1);

        Student student5 = new Student("Богдан");
        student5.setEvents(events3);

        Group group3 = new Group();
        group3.setStudents(List.of(student4, student5));

        // Собираем всё в массив
        Group[] groups = new Group[3];
        groups[0] = group1;
        groups[1] = group2;
        groups[2] = group3;

        //Course course = new Course(groups);
        //course.generateUniquePassword();

        String password = CmanSecurity.generatePassword();

        File file = new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\TestSecurity.cman");

        // Сохраняем
        try {
            save(groups, file, "123456789#");
            /*decryptToFile(new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\TestSecurity.cman"),
                    new File("C:\\Users\\Nazar\\.cmanfx\\Courses\\TestSecurityDecrypted.cman"),
                    "0w%J8c&pU)");*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}