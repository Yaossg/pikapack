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
- Operation
  - `--refresh` copy files from src to dst, default operation
  - `--restore` copy files from dst to src
- Behavior
  - default behavior: no compression or encryption
  - `--compress` compress during refresh and decompress during restore
  - `--encrypt` encrypt during compress and decrypt during decompress
- Service
  - `--watch` watch changes from filesystem
  - `-sched <interval>` scheduled synchronization 
- Filter
  - `-excl <glob>` filter files to exclude, excludes none by default
  - `-incl <glob>` filter files to include, includes all by default
  - exclusion filter happens before inclusion filter
