package io.github.edadma.freetype

@main def run(): Unit =
  val library = initFreeType.getOrElse(sys.error("error initializing library"))
  val face = library.newFace("KaiseiDecol/KaiseiDecol-Regular.ttf", 0).getOrElse(sys.error("error loading face"))

  if face.setPixelSizes(0, 16) != 0 then sys.error("error setting size")

  for ch <- 'A' to 'C' do
    if face.loadChar(ch.toInt, 0) != 0 then sys.error("error loading glyph")
    if face.renderGlyph(RenderMode.NORMAL) != 0 then sys.error("error rendering glyph")

    println(ch)
    println(face.bitmap.rows)
    println(face.bitmap.width)
    println(face.bitmap.pitch)
