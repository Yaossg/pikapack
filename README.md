# Pikapack

## Usage

### GUI Mode

```sh
java -jar pikapack.jar
```

### Console Mode

```sh
java -jar pikapack.jar <options...>
```

#### Options

- `-src <path>` specify source folder
- `-dst <path>` specify destination folder
- Operations
  - `--refresh` copy files from src to dst, default operation
  - `--restore` copy files from dst to src
- Behaviours
  - `--compress` compress during refresh and decompress during restore
  - `--encrypt` encrypt during refresh and decrypt during restore
  - `--watch` watch changes from filesystem
  - `-sched <interval>` scheduled synchronization 
  - `-incl <predicate>` filter files to include
  - `-excl <predicate>` filter files to exclude
