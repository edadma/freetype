freetype
========

Scala Native bindings for [FreeType](https://freetype.org/), a freely available software library to render fonts.

Installation
------------

Add the dependency to your `build.sbt`:

```scala
libraryDependencies += "io.github.edadma" %%% "freetype" % "0.0.3"
```

You also need FreeType installed on your system:

```bash
# Ubuntu/Debian
sudo apt install libfreetype-dev

# macOS
brew install freetype
```

Usage
-----

```scala
import io.github.edadma.freetype.*

// Initialize FreeType library
val library = initFreeType.getOrElse(sys.error("Failed to init FreeType"))

// Load a font face
val face = library.newFace("/path/to/font.ttf", 0).getOrElse(sys.error("Failed to load font"))

// Set pixel size
face.setPixelSizes(0, 24)

// Get kerning between two characters (requires setPixelSizes first)
val kern = face.getKerning('A', 'V')  // Returns scaled value in pixels

// Or get unscaled kerning in font design units
val kernUnscaled = face.getKerning('A', 'V', KerningMode.UNSCALED)

// Load and render a glyph
face.loadChar('A'.toLong, 0)
face.renderGlyph(RenderMode.NORMAL)

// Access bitmap data
val bmp = face.bitmap
println(s"${bmp.width} x ${bmp.rows}")

// Cleanup
face.doneFace
library.doneFreeType
```

API
---

### Library

- `initFreeType: Either[Int, Library]` - Initialize FreeType
- `Library.doneFreeType: Int` - Cleanup
- `Library.newFace(path: String, faceIndex: Long): Either[Int, Face]` - Load a font

### Face

- `Face.setPixelSizes(width: Int, height: Int): Int` - Set pixel size
- `Face.loadChar(charCode: Long, loadFlags: Int): Int` - Load a glyph
- `Face.renderGlyph(mode: RenderMode): Int` - Render loaded glyph
- `Face.bitmap: Bitmap` - Access rendered bitmap
- `Face.getCharIndex(charcode: Long): Int` - Get glyph index for character
- `Face.getKerning(left: Char, right: Char, mode: KerningMode): Double` - Get kerning

### Enums

- `RenderMode`: NORMAL, LIGHT, MONO, LCD, LCD_V, SDF, MAX
- `KerningMode`: DEFAULT (scaled, grid-fitted), UNFITTED (scaled), UNSCALED (font units)

License
-------

ISC
