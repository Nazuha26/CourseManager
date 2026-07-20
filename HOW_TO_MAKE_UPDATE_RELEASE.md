# Выпуск новой версии CourseManagerFX

## 1. Подготовить программу

1. Указать новую версию в `pom.xml`, например `1.2.5`.
2. В корне проекта создать app-image:

```powershell
.\build-tools\build-app-image.bat
```

Готовая программа будет находиться в папке:

```text
build-output\app-image\CourseManagerFX
```

## 2. Создать MSI-установщик (если нужно)

В корне проекта выполнить:

```powershell
.\build-tools\build-msi.bat
```

Готовый установщик будет находиться в папке:

```text
build-output\msi
```

## 3. Создать файлы обновления

Приватный ключ находится здесь:

```text
.\PrivateKeys\CourseManagerFX-release-private-key.pk8
```

В корне проекта выполнить:

```powershell
.\build-tools\build-update-release.ps1
```

Скрипт создаст в папке `build-output\release` два файла:

```text
CourseManagerFX_v1.2.5.zip
CourseManagerFX_v1.2.5.zip.sig
```

## 4. Опубликовать релиз на GitHub

1. Открыть страницу репозитория `Releases` → `Draft a new release`.
2. Создать новый тег `v1.2.5` из ветки `main`.
3. Указать название релиза `CourseManagerFX v1.2.5`.
4. Перетащить в раздел файлов оба файла из `build-output\release`:
    - `CourseManagerFX_v1.2.5.zip`;
    - `CourseManagerFX_v1.2.5.zip.sig`.
5. Для обычной стабильной версии не включать `Set as a pre-release`.
6. Выбрать `Set as the latest release` и нажать `Publish release`.

> Версия в `pom.xml`, теге и именах загружаемых файлов должна совпадать.
