# Railroads - Distributed Version

## Requirements

- Java
- Maven
- MPJ Express 0.44

Set the `MPJ_HOME` environment variable to the folder where MPJ Express is installed. 

### Example on macOS/Linux: 
```bash
export MPJ_HOME=~/mpj-v0_44
```


### Example on Windows:
```
set MPJ_HOME=C:\mpj-v0_44
```

## Run distributed mode
Build the project in IntelliJ.

The launcher automatically detects the available logical CPU cores.

### macOS/Linux
Run once to make the script executable:

```
chmod +x run_distributed.sh
```

Then run:

```
./run_distributed.sh
```

### Windows
Run:
```
run_distributed.bat
```