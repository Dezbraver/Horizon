# Horizon

Horizon is an open source tool that integrates the processing capabilities of Volatility 3 into IPED.

## Purposes

1. Automate procedures performed in Volatility 3.
2. Take advantage of IPED's graphical interface for results management.
3. Allow new integrations to be created by the community using plugins.

## How to?

Please check our [Wiki](https://github.com/Dezbraver/Horizon/wiki) for more instructions.

## Integrations Performed

### Windows Plugins

- DumpFilesExtractor (windows.pslist.PsList --dump)
- EnvarsFilesExtractor (windows.envars.Envars)
- CmdLineExtractor (windows.cmdline.CmdLine)
- NetScanExtractor (windows.netscan.NetScan)
- DllListFilesExtractor (windows.dlllist.DllList)
- DriverScanFileExtractor (windows.driverscan.DriverScan)
- PrivsFilesExtractor (windows.privileges.Privs)
- HiveListFileExtractor (windows.registry.hivelist.HiveList)
- PrintKeyFileExtractor (windows.registry.printkey.PrintKey)
- SkeletonKeyCheckFileExtractor (windows.skeleton_key_check.Skeleton_Key_Check)
- SymlinkScanFileExtractor (windows.symlinkscan.SymlinkScan)

### Linux Plugins

- BashFileExtractor (linux.bash.Bash)
- LCheckSyscallFileExtractor (linux.check_syscall.Check_syscall)
- ElfsFilesExtractor (linux.elfs.Elfs)
- LLsmodFileExtractor (linux.lsmod.Lsmod)
- LsofFilesExtractor (linux.lsof.Lsof)
- MapsFilesExtractor (linux.proc.Maps)
- TTYCheckFileExtractor (linux.tty_check.tty_check)

### MacOS Plugins

- PsauxFilesExtractor (mac.psaux.Psaux)
- MLsmodFileExtractor (mac.lsmod.Lsmod)
- MCheckSyscallFileExtractor (mac.check_syscall.Check_syscall)
- IfconfigFileExtractor (mac.ifconfig.Ifconfig)
- KeventsFilesExtractor (mac.kevents.Kevents)
- MountFileExtractor (mac.mount.Mount)
- NetstatFilesExtractor (mac.netstat.Netstat)

## License

This project is licensed under the GNU General Public License v3.0.
