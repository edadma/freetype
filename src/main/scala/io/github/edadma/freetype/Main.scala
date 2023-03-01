package io.github.edadma.freetype

@main def run(): Unit =
  val library = initFreeType.getOrElse(sys.error("error initializing library"))
  val face = library.newFace("KaiseiDecol/KaiseiDecol-Regular.ttf", 0).getOrElse(sys.error("error loading face"))

  if face.setPixelSizes(0, 16) != 0 then sys.error("error setting size")

  for ch <- 'A' to 'C' do
    if face.loadChar(ch.toInt, 0) != 0 then sys.error("error loading glyph")
    if face.renderGlyph(RenderMode.NORMAL) != 0 then sys.error("error rendering glyph")

    println(ch)

    val pitch = face.bitmap.pitch

    for i <- 0 until face.bitmap.rows do
      for j <- 0 until face.bitmap.width do print(f"${face.bitmap.buffer(i * pitch + j)}%3d ")

      println
    end for

    println
