Option Explicit

Dim fso, shell, folderPath, folderObj, files, file
Dim exeName, exePath

Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")
folderPath = fso.GetParentFolderName(WScript.ScriptFullName)

' ===== STEP 1: Find the exe file =====
Set folderObj = fso.GetFolder(folderPath)
Set files = folderObj.Files
exePath = ""


For Each file In files
    If LCase(fso.GetExtensionName(file.Name)) = "exe" Then
        If InStr(LCase(file.Name), "coursemanagerfx") > 0 Then
            exePath = file.Path
            exeName = file.Name
            Exit For
        End If
    End If
Next

If exePath = "" Then
    MsgBox "Executable with 'CourseManagerFX' not found in: " & folder, vbCritical, "ERROR EXE NOT FOUND"
    WScript.Quit 1
End If

' ===== STEP 2: Wait until all CourseManagerFX processes finish =====
WaitUntilCourseManagerStops

Sub WaitUntilCourseManagerStops()
    Dim wmi, processes, proc, stillRunning
    Set wmi = GetObject("winmgmts:\\.\root\cimv2")

    Do
        stillRunning = False
        Set processes = wmi.ExecQuery("SELECT * FROM Win32_Process")

        For Each proc In processes
            If InStr(LCase(proc.Name), "coursemanagerfx") > 0 Then
                stillRunning = True
                Exit For
            End If
        Next

        If stillRunning Then
            WScript.Sleep 500  ' wait 0.5 second
        End If
    Loop While stillRunning
End Sub

' ===== STEP 3: Start the exe =====
shell.Run """" & exePath & """", 0