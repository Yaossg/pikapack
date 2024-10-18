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
  - `--refresh` sync files from src to dst, default operation
  - `--restore` sync files from dst to src
- Sync Behavior
  - `--copy` copy all files and folders unchanged, default behavior
  - `--pack` pack all files and folders into one file
  - `--compress` compress/decompress packed file
  - `--encrypt` encrypt/decrypt packed file
  - `-key <key>` specify encryption key
- Service
  - `--watch` watch changes from filesystem
  - `-sched <interval>` scheduled synchronization 
- Filter
  - `-excl <glob>` filter files to exclude, excludes none by default
  - `-incl <glob>` filter files to include, includes all by default
  - exclusion filter happens before inclusion filter
