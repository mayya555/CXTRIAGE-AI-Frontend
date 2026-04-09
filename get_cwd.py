
import subprocess

pid = 8504
try:
    # Use PowerShell to get the CWD of a process
    # This requires specific permissions, but let's try
    cmd = f'(Get-Process -Id {pid}).Path'
    result = subprocess.check_output(['powershell', '-Command', cmd], universal_newlines=True)
    print(f"PATH: {result.strip()}")
    
    # Another way to get CWD specifically
    cmd_cwd = f'(Get-CimInstance Win32_Process -Filter "ProcessId = {pid}").CommandLine'
    print(f"FULL CMD: {subprocess.check_output(['powershell', '-Command', cmd_cwd], universal_newlines=True).strip()}")
    
except Exception as e:
    print(f"Error: {e}")
