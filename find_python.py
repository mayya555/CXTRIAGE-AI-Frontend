
import os
import subprocess

try:
    # Use PowerShell to get process details including command line
    cmd = 'Get-WmiObject Win32_Process -Filter "name = \'python.exe\'" | Select-Object CommandLine, ProcessId | Format-List'
    result = subprocess.check_output(['powershell', '-Command', cmd], universal_newlines=True)
    print(result)
except Exception as e:
    print(f"Error: {e}")
