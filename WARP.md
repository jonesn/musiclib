# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

**musiclib** is a Clojure CLI tool for managing music libraries. It analyzes music directory structures, generates statistics, identifies orphaned files (files not following the expected hierarchy), and can compare two music libraries. The tool supports MP3, FLAC, M4A, and M3U formats.

Key features:
- Generates library statistics (artist count, album count, file counts by format)
- Identifies "orphaned" files that don't follow the `artist/album/song` hierarchy
- Fixes orphaned MP3 files by reading ID3 tags and moving them to correct locations
- Diffs two music libraries to show what's unique to each
- Validates corrupted FLAC files (see README for command)

## Dependencies & Installation

This project uses Leiningen for build management. Install Leiningen via sdkman (user preference):
```bash
sdk install leiningen
```

## Build & Development Commands

### Building
```bash
# Build standalone uberjar
lein uberjar

# Clean build artifacts
lein clean
```

The uberjar will be created at `target/uberjar/musiclib-0.1.0-SNAPSHOT-standalone.jar`

### Running the Application
```bash
# Show help
lein run -- -h

# Generate statistics for a music library
lein run -- -p /path/to/Music

# Diff two music libraries
lein run -- -p ~/Music -q /path/to/other/Music

# Fix orphaned files (moves files based on ID3 tags)
lein run -- -p /path/to/Music -f true

# List all albums in artist/album format
lein run -- -p /path/to/Music -l true

# Using the built uberjar
java -jar target/uberjar/musiclib-0.1.0-SNAPSHOT-standalone.jar -p /path/to/Music
```

### Testing
```bash
# Run all tests
lein test

# Run specific test namespace
lein test nz.co.arachnid.musiclib.musiclib-test
```

### Code Formatting
The project uses cljfmt (configured in project.clj):
```bash
# Check code formatting
lein cljfmt check

# Fix code formatting
lein cljfmt fix
```

### REPL Development
```bash
# Start REPL
lein repl
```

Utility functions available in the `user` namespace (see `user/user.clj`):
- `(list-namespaces)` - Lists all namespaces on classpath
- `(list-public-symbols 'namespace)` - Lists public symbols for a namespace
- `(repl-reload)` - Refreshes changed namespaces using tools.namespace

Example REPL workflow from `core.clj`:
```clojure
(def path "/path/to/music")
(def lib-a (generate-music-lib path))
(def stats (generate-library-stats lib-a))
(def orphan-seq (extract-orphan-meta-data lib-a))
```

## Code Architecture

The codebase is organized into five main namespaces with clear separation of concerns:

### Namespace Structure

1. **`nz.co.arachnid.musiclib.domain`** - Core data structures and specifications
   - Defines constants (file extensions, path separators, ORPHAN marker)
   - Records: `ArtistSongSet` (artist, album, root-file, song-set, format) and `LibStats`
   - clojure.spec definitions for validation
   - Format detection logic (`:mp3`, `:flac`, `:m3u`, `:m4a`, `:not-supported`)

2. **`nz.co.arachnid.musiclib.search`** - Library generation and analysis
   - `generate-music-lib` - Walks directory tree and creates library representation
   - `generate-library-stats` - Aggregates statistics from library
   - `diff-libs` - Compares two libraries and returns unique albums
   - `group-lib-by-artist-album` - Groups records for reporting
   - Uses `me.raynes.fs/walk` for filesystem traversal

3. **`nz.co.arachnid.musiclib.mp3`** - MP3 metadata extraction
   - `extract-metadata-from-mp3` - Reads ID3v2 tags using mp3agic library
   - `construct-destination-path` - Builds correct path from metadata
   - `extract-orphan-meta-data` - Pipeline for finding and preparing orphan MP3s for fixing
   - Only processes MP3 orphans (FLAC orphans are identified but not auto-fixed)

4. **`nz.co.arachnid.musiclib.filesystem`** - File operations
   - `fix-orphans-in-lib!` - Orchestrates moving orphaned files to correct locations
   - `create-directories-if-not-present` - Ensures target directories exist
   - Side-effecting operations are marked with `!` convention

5. **`nz.co.arachnid.musiclib.core`** - CLI and orchestration
   - Main entry point (`-main`)
   - Command-line parsing using `clojure.tools.cli`
   - Output formatting with colored terminal output (`clojure.term.colors`)
   - `run` function coordinates the workflow

### Data Flow

1. **Library Generation**: `core/run` → `search/generate-music-lib` → `domain/create-artist-song-set`
   - Walks directory tree, identifying artist/album/song hierarchy
   - Files not following hierarchy are marked as "Orphan"

2. **Statistics**: `search/generate-library-stats` counts artists, albums, and files by format

3. **Orphan Fixing** (MP3 only):
   - `mp3/extract-orphan-meta-data` extracts ID3 tags from orphaned MP3s
   - `search/group-lib-by-artist-album` groups by target location
   - `filesystem/fix-orphans-in-lib!` creates directories and moves files

4. **Library Diffing**: `search/diff-libs` compares on artist/album/format keys

### Expected Music Directory Structure

The tool expects a three-level hierarchy:
```
/Music
  /Artist Name
    /Album Name
      01 - Song Title.mp3
      02 - Song Title.flac
```

Files found at any other level are marked as "Orphan". Only MP3 orphans with valid ID3v2 tags can be auto-fixed (moved to correct location based on metadata).

### Key Design Patterns

- **clojure.spec validation**: All core data structures are spec'd and validated
- **Immutable data**: Library is represented as a set of immutable `ArtistSongSet` records
- **Threading macros**: Heavy use of `->>` for data transformation pipelines
- **Functional core, imperative shell**: Pure functions for logic, side effects isolated in `filesystem` namespace
- **Side-effect marking**: Functions with side effects use `!` suffix convention

## Testing

Tests are in `test/nz/co/arachnid/musiclib/musiclib_test.clj` and cover:
- File extension filtering and format detection
- Library statistics generation
- Library diffing logic
- Test fixtures define sample albums (Mastodon, Nirvana) for testing

Run tests with `lein test` during development.
