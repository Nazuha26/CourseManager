Option Explicit

Const READY_ARGUMENT_PREFIX = "--coursemanagerfx-update-ready="

Dim fso, shell, arguments
Set fso = CreateObject("Scripting.FileSystemObject")
Set shell = CreateObject("Shell.Application")
Set arguments = WScript.Arguments

If arguments.Count < 7 Then
    WScript.Quit 2
End If

Dim payloadDir, installDir, readyMarker, cancelledMarker
Dim successMarker, appPid, version, elevated

payloadDir = fso.GetAbsolutePathName(arguments(0))
installDir = fso.GetAbsolutePathName(arguments(1))
readyMarker = fso.GetAbsolutePathName(arguments(2))
cancelledMarker = fso.GetAbsolutePathName(arguments(3))
successMarker = fso.GetAbsolutePathName(arguments(4))
appPid = CLng(arguments(5))
version = arguments(6)
elevated = False
If arguments.Count >= 8 Then
    elevated = (LCase(CStr(arguments(7))) = "--elevated")
End If

If Not elevated Then
    RequestElevation payloadDir, installDir, readyMarker, cancelledMarker, _
            successMarker, appPid, version
    WScript.Quit 0
End If

Dim scriptRoot, logFile
scriptRoot = fso.GetParentFolderName(WScript.ScriptFullName)
logFile = fso.BuildPath(scriptRoot, "update.log")

WriteMarker readyMarker, "approved"
WriteLog "Updater v" & version & " received administrator permission."

If Not WaitForProcessExit(appPid, 300) Then
    WriteLog "The old application did not exit within five minutes."
    RelaunchExisting installDir
    WScript.Quit 3
End If

Dim nextDir, oldDir, movedOld
nextDir = installDir & ".new"
oldDir = installDir & ".old"
movedOld = False

On Error Resume Next

DeleteFolderIfPresent nextDir
If Err.Number <> 0 Then FailUpdate "Could not remove a previous .new directory", _
        installDir, oldDir, movedOld

DeleteFolderIfPresent oldDir
If Err.Number <> 0 Then FailUpdate "Could not remove a previous .old directory", _
        installDir, oldDir, movedOld

Err.Clear
fso.CopyFolder payloadDir, nextDir, True
If Err.Number <> 0 Then FailUpdate "Could not copy the new application", _
        installDir, oldDir, movedOld

If Not IsValidAppImage(nextDir) Then
    FailUpdate "The copied application image is incomplete", _
            installDir, oldDir, movedOld
End If

Err.Clear
fso.MoveFolder installDir, oldDir
If Err.Number <> 0 Then FailUpdate "Could not back up the current installation", _
        installDir, oldDir, movedOld
movedOld = True

Err.Clear
fso.MoveFolder nextDir, installDir
If Err.Number <> 0 Then FailUpdate "Could not move the new installation into place", _
        installDir, oldDir, movedOld

WriteLog "New application files installed. Starting v" & version & "."
Err.Clear
shell.ShellExecute fso.BuildPath(installDir, "CourseManagerFX.exe"), _
        Quote(READY_ARGUMENT_PREFIX & successMarker), installDir, "open", 1
If Err.Number <> 0 Then FailUpdate "Could not start the new application", _
        installDir, oldDir, movedOld

On Error GoTo 0

If WaitForFile(successMarker, 90) Then
    WriteLog "New application confirmed a successful start."
    On Error Resume Next
    DeleteFolderIfPresent oldDir
    If Err.Number <> 0 Then
        WriteLog "The old installation could not be removed: " & Err.Description
    End If
    On Error GoTo 0
    WScript.Quit 0
End If

On Error Resume Next
FailUpdate "The new application did not confirm startup", _
        installDir, oldDir, movedOld

Sub RequestElevation(payload, installation, ready, cancelled, successful, pid, ver)
    Dim elevationArguments
    elevationArguments = Quote(WScript.ScriptFullName) & " " _
            & Quote(payload) & " " _
            & Quote(installation) & " " _
            & Quote(ready) & " " _
            & Quote(cancelled) & " " _
            & Quote(successful) & " " _
            & Quote(CStr(pid)) & " " _
            & Quote(ver) & " --elevated"

    On Error Resume Next
    Err.Clear
    shell.ShellExecute "wscript.exe", elevationArguments, "", "runas", 0
    If Err.Number <> 0 Then
        WriteMarker cancelled, "cancelled"
    End If
    On Error GoTo 0
End Sub

Sub FailUpdate(message, installation, backup, backupWasCreated)
    Dim details
    details = message
    If Err.Number <> 0 Then details = details & ": " & Err.Description
    WriteLog "ERROR: " & details
    Err.Clear

    If backupWasCreated And fso.FolderExists(backup) Then
        StopApplication fso.BuildPath(installation, "CourseManagerFX.exe")
        WScript.Sleep 1000

        If fso.FolderExists(installation) Then
            fso.DeleteFolder installation, True
        End If
        If Not fso.FolderExists(installation) Then
            fso.MoveFolder backup, installation
            WriteLog "Previous installation restored."
        End If
    End If

    RelaunchExisting installation
    WScript.Quit 4
End Sub

Sub RelaunchExisting(installation)
    Dim executable
    executable = fso.BuildPath(installation, "CourseManagerFX.exe")
    If fso.FileExists(executable) Then
        On Error Resume Next
        shell.ShellExecute executable, "", installation, "open", 1
        On Error GoTo 0
    End If
End Sub

Sub StopApplication(executable)
    Dim service, processes, process
    On Error Resume Next
    Set service = GetObject("winmgmts:\\.\root\cimv2")
    Set processes = service.ExecQuery( _
            "SELECT * FROM Win32_Process WHERE Name='CourseManagerFX.exe'")
    For Each process In processes
        If LCase(process.ExecutablePath) = LCase(executable) Then
            process.Terminate
        End If
    Next
    On Error GoTo 0
End Sub

Function WaitForProcessExit(pid, timeoutSeconds)
    Dim attempt
    For attempt = 1 To timeoutSeconds * 5
        If Not IsProcessRunning(pid) Then
            WaitForProcessExit = True
            Exit Function
        End If
        WScript.Sleep 200
    Next
    WaitForProcessExit = False
End Function

Function IsProcessRunning(pid)
    Dim service, processes
    On Error Resume Next
    Set service = GetObject("winmgmts:\\.\root\cimv2")
    Set processes = service.ExecQuery( _
            "SELECT ProcessId FROM Win32_Process WHERE ProcessId=" & CStr(pid))
    IsProcessRunning = processes.Count > 0
    If Err.Number <> 0 Then IsProcessRunning = True
    On Error GoTo 0
End Function

Function WaitForFile(filePath, timeoutSeconds)
    Dim attempt
    For attempt = 1 To timeoutSeconds * 5
        If fso.FileExists(filePath) Then
            WaitForFile = True
            Exit Function
        End If
        WScript.Sleep 200
    Next
    WaitForFile = False
End Function

Function IsValidAppImage(folder)
    IsValidAppImage = _
            fso.FileExists(fso.BuildPath(folder, "CourseManagerFX.exe")) _
            And fso.FileExists(fso.BuildPath(folder, "app\CourseManagerFX.jar")) _
            And fso.FileExists(fso.BuildPath(folder, "runtime\bin\java.exe"))
End Function

Sub DeleteFolderIfPresent(folder)
    If fso.FolderExists(folder) Then
        fso.DeleteFolder folder, True
    End If
End Sub

Sub WriteMarker(filePath, text)
    Dim stream
    On Error Resume Next
    Set stream = fso.CreateTextFile(filePath, True, False)
    stream.WriteLine text
    stream.Close
    On Error GoTo 0
End Sub

Sub WriteLog(text)
    Dim stream
    On Error Resume Next
    Set stream = fso.OpenTextFile(logFile, 8, True, 0)
    stream.WriteLine CStr(Now) & "  " & text
    stream.Close
    On Error GoTo 0
End Sub

Function Quote(value)
    Quote = Chr(34) & Replace(CStr(value), Chr(34), Chr(34) & Chr(34)) & Chr(34)
End Function
