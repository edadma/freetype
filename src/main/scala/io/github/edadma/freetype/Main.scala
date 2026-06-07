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

  // Variation (variable-font) API. KaiseiDecol is a static font, so this exercises the
  // not-variable path; a variable font would list its axes and ranges here.
  face.getMMVar match
    case Right(mm) =>
      println(s"variable font: ${mm.numAxis} axes")
      for i <- 0 until mm.numAxis do
        val a = mm.axis(i)
        println(f"  ${a.tagString} (${a.name}): ${a.minimum}%.1f .. ${a.default}%.1f .. ${a.maximum}%.1f")
      library.doneMMVar(mm)
    case Left(err) =>
      println(s"not a variable font (FT_Get_MM_Var: ${errorString(err)})")
