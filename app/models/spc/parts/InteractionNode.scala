package models.spc.parts

import scala.xml.Node

final case class InteractionNode(
                                  category: InteractionCategory,
                                  event:    InteractionEvent,
                                  prompt:   InteractionPrompt,
                                  name:     String              = InteractionNode.name) extends SpcXmlNode {

  def toXml: Node = {
    <INTERACTION name="posDisplayMessage">
      <STATUS category={ category.toString } event={ event.toString }/>
      <PROMPT>{ prompt.toString }</PROMPT>
    </INTERACTION>
  }
}

object InteractionNode {

  val name = "InteractionNode"
}
