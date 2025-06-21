
' cman_updater.vbs
Option Explicit

Dim shell, fso, args, targetPath
Set shell = CreateObject("WScript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set args = WScript.Arguments

' ====== STEP 1: get target path from arguments ======
If args.Count < 1 Then
    MsgBox "No target path specified.", vbCritical, "Updater Error"
    WScript.Quit 1
End If

targetPath = args(0)

' ====== STEP 2: check for admin rights ======
If Not IsAdmin() Then
' Relaunch self with admin rights
    shell.Popup "There is no admin rights. Restarting...", 3, "Updater Warning", 48 + 4096
    shell.ShellExecute "wscript.exe", """" & WScript.ScriptFullName & """ """ & targetPath & """", "", "runas", 1
    WScript.Quit
End If

' ====== STEP 3: close all running coursemanagerfx processes ======
KillCourseManagerProcesses

' ====== STEP 4: copy files from current dir to target path ======
Dim updateFolder
updateFolder = fso.BuildPath(fso.GetParentFolderName(WScript.ScriptFullName), "update")

If Not fso.FolderExists(updateFolder) Then
    MsgBox "Update folder not found: " & updateFolder, vbCritical, "Updater Error"
    WScript.Quit 1
End If

CopyUpdateContent updateFolder, targetPath

shell.Popup "Update installed successfully, CourseManagerFX will start automatically.", 5, "Updater Success", 64 + 4096

' === run automatically
Dim exePath, fileFound
fileFound = False

Dim file
For Each file In fso.GetFolder(targetPath).Files
    If LCase(fso.GetExtensionName(file.Name)) = "exe" Then
        If InStr(LCase(file.Name), "coursemanagerfx") > 0 Then
            exePath = file.Path
            fileFound = True
            Exit For
        End If
    End If
Next

If fileFound Then
    shell.Run """" & exePath & """", 1, False
Else
    MsgBox "Executable file not found. Please start CourseManagerFX manually.", vbCritical, "Updater Error"
End If

' delete temporary update folder
On Error Resume Next
Dim tmpDir: tmpDir = fso.GetParentFolderName(WScript.ScriptFullName)
fso.DeleteFolder tmpDir, True
On Error GoTo 0

' ====== FUNCTIONS ======

Function IsAdmin()
    On Error Resume Next
    Dim testFile
    testFile = shell.ExpandEnvironmentStrings("%SYSTEMROOT%") & "\system32\test_admin.txt"
    fso.CreateTextFile(testFile).Write "test"
    If Err.Number <> 0 Then
        IsAdmin = False
        Err.Clear
    Else
        fso.DeleteFile testFile
        IsAdmin = True
    End If
    On Error GoTo 0
End Function

Sub KillCourseManagerProcesses()
    Dim wmi, processes, proc
    Set wmi = GetObject("winmgmts:\\.\root\cimv2")
    Set processes = wmi.ExecQuery("SELECT * FROM Win32_Process")

    For Each proc In processes
        If InStr(LCase(proc.Name), "coursemanagerfx") > 0 Then
            On Error Resume Next
            proc.Terminate
            On Error GoTo 0
        End If
    Next
End Sub



Sub CopyUpdateContent(fromFolder, toFolder)
    If Not fso.FolderExists(toFolder) Then
        MsgBox "Target path not found: " & toFolder, vbCritical, "Updater Error"
        Exit Sub
    End If

    Dim file
    For Each file In fso.GetFolder(fromFolder).Files
        fso.CopyFile file.Path, fso.BuildPath(toFolder, fso.GetFileName(file)), True
    Next

    Dim subfolder
    For Each subfolder In fso.GetFolder(fromFolder).SubFolders
        CopyFolderRecursive subfolder.Path, fso.BuildPath(toFolder, fso.GetFileName(subfolder))
    Next
End Sub


Sub CopyFolderRecursive(fromFolder, toFolder)
    If Not fso.FolderExists(toFolder) Then
        fso.CreateFolder toFolder
    End If

    Dim file
    For Each file In fso.GetFolder(fromFolder).Files
        fso.CopyFile file.Path, fso.BuildPath(toFolder, fso.GetFileName(file)), True
    Next

    Dim subfolder
    For Each subfolder In fso.GetFolder(fromFolder).SubFolders
        CopyFolderRecursive subfolder.Path, fso.BuildPath(toFolder, fso.GetFileName(subfolder))
    Next
End Sub