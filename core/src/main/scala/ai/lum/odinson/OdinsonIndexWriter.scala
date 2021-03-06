package ai.lum.odinson

import java.io.File
import java.nio.file.Path
import java.util.Collection
import scala.collection.JavaConverters._
import org.apache.lucene.document.Document
import org.apache.lucene.analysis.core.WhitespaceAnalyzer
import org.apache.lucene.index.{ IndexWriter, IndexWriterConfig }
import org.apache.lucene.index.IndexWriterConfig.OpenMode
import org.apache.lucene.store.FSDirectory
import ai.lum.odinson.digraph.{ DirectedGraph, Vocabulary }

class OdinsonIndexWriter(
  val indexDir: Path,
  val vocabularyFile: File
) {

  def this(indexDir: File, vocabularyFile: File) = {
    this(indexDir.toPath, vocabularyFile)
  }

  // dependencies vocabulary
  val vocabulary = Vocabulary.empty

  val dir = FSDirectory.open(indexDir)
  val analyzer = new WhitespaceAnalyzer()
  val writerConfig = new IndexWriterConfig(analyzer)
  writerConfig.setOpenMode(OpenMode.CREATE)
  val writer = new IndexWriter(dir, writerConfig)

  def addDocuments(block: Seq[Document]): Unit = {
    addDocuments(block.asJava)
  }

  def addDocuments(block: Collection[Document]): Unit = {
    writer.addDocuments(block)
  }

  def close(): Unit = {
    vocabulary.dumpToFile(vocabularyFile)
    writer.close()
  }

  def mkDirectedGraph(
    incomingEdges: Array[Array[(Int, String)]],
    outgoingEdges: Array[Array[(Int, String)]],
    roots: Array[Int]
  ): DirectedGraph = {
    def toLabelIds(tokenEdges: Array[(Int, String)]): Array[Int] = for {
      (tok, label) <- tokenEdges
      labelId = vocabulary.getOrCreateId(label)
      n <- Array(tok, labelId)
    } yield n
    val incoming = incomingEdges.map(toLabelIds)
    val outgoing = outgoingEdges.map(toLabelIds)
    DirectedGraph(incoming, outgoing, roots)
  }

}
