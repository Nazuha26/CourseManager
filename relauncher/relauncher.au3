If $CmdLine[0] = 0 Then
    MsgBox(16, "Missing Argument", "Executable path not provided!")
    Exit
EndIf

; ===== STEP 1: Find the exe file =====
Global $exePath = $CmdLine[1]

If Not FileExists($exePath) Then
    MsgBox(16, "EXE NOT FOUND", "Executable not found at path: " & $exePath)
    Exit
EndIf

; ===== STEP 2: Wait until all CourseManagerFX processes finish =====
WaitUntilCourseManagerStops()

Func WaitUntilCourseManagerStops()
    ; checking if the process is running "CourseManagerFX.exe"
    While ProcessExists("CourseManagerFX.exe")
        Sleep(500) ; wait 0.5 second
    WEnd
EndFunc

; ===== STEP 3: Start the exe =====
; @SW_HIDE is hide mod
Run('"' & $exePath & '"', "", @SW_HIDE)