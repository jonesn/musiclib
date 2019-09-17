# Music Lib

Print Statistics and Orphaned File Usage in Music Library

 - Summary By Extension Type
 - Total Library Size and by extension
 - Nice colours, its a report.
 - Fix mode for orphaned files.
 - Compare local Music with Pono player. Show Diffs.
 
## Examples

```bash
λ megadrive musiclib → λ git master* → java -jar target/uberjar/musiclib-0.1.0-SNAPSHOT-standalone.jar 
Picked up _JAVA_OPTIONS: -Xmx2048m
This program prints the statistics of the tree of Music under the given path.
It also provides the option to try and correct files that don't fall into the
         artist/album/song hierarchy.

Example Usage Windows: musicLib -p C:\Users\Nick Jones\Music
Example Usage Linux:   musicLib -p /home/jonesn/Music
java -jar C:\dev\arachnid\clojure\musiclib\target\uberjar\musiclib-0.1.0-SNAPSHOT-standalone.jar --path "C:\Users\Nick Jones\Music" --fix true

Options:
  -h, --help
  -p, --path  The parent directory to scan I.e.
  -f, --fix   Should orphaned files be moved to the correct place in the file system.
```

### Bugs
