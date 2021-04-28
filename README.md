# services-metagetter
Quick app to add media metadata to DLCS CSV files

Java app that parses DLCS CSV files, extracts paths for A/V media files, uses ffmpeg to extract metadata (height/width, duration, and MIME type), and adds the metadata to the CSV.

Maven 3.x or later is required to build the project (`mvn clean package` to build, executable JAR wull be created in target/build-artifact/).

[ffmpeg](https://www.ffmpeg.org/) 3.x or later required to run `metagetter`.

Java 11 or later required to build and run `metagetter`

To run the app: `java -jar /path/to/services-metagetter-[version].jar /path/to/csv(s) /path/to/mountpount(s) /path/to/ffprobe /path/to/output/directory/`
`metagetter` can process multiple CSVs at a time, so the `/path/to/csv(s)` can be a directory.
`/path/to/mountpoints(s)` can be a comma-separated list, if media files may be located in multiple mounts.
`/path/to/ffprobe` needs to point to the actual `ffprobe` executable (or alias for it defined on PATH or OS equivalent).
Error messages (e.g., errors reading CSVs or media files) are sent to the system `err` ouput.
