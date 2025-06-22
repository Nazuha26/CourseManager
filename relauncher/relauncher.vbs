Option Explicit

If WScript.Arguments.Count = 0 Then
    MsgBox "Executable path not provided!", vbCritical, "Missing Argument"
    WScript.Quit 1
End If

Dim fso, shell, exePath

Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("WScript.Shell")

' ===== STEP 1: Find the exe file =====

exePath = WScript.Arguments(0)
If Not fso.FileExists(exePath) Then
    MsgBox "Executable not found at path: " & exePath, vbCritical, "EXE NOT FOUND"
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