package com.radeusgd.archivum.datamodel

import com.radeusgd.archivum.datamodel.types.{EnumType, FieldType, LongString, SimpleString}
import spray.json._
import sun.reflect.generics.reflectiveObjects.NotImplementedException

object ModelJsonProtocol extends DefaultJsonProtocol {
   implicit object ModelDefinitionJsonFormat extends RootJsonFormat[ModelDefinition] {
      override def write(obj: ModelDefinition): JsValue = {
         throw new NotImplementedError("Currently serializing model definition back to JSON is not supported.")
      }

      override def read(json: JsValue): ModelDefinition = {
         json.asJsObject.getFields("name", "types", "fields") match {
            case Seq(JsString(name), JsObject(types), JsObject(fields)) =>
               val customTypes: Map[String, FieldType] = types map (readEnumDef _).tupled
               new ModelDefinition(name, fields mapValues readField(customTypes))
            case _ => throw DeserializationException("Wrong model root structure")
         }
      }
   }

   private def readEnumDef(name: String, json: JsValue): (String, FieldType) = {
      json match {
         case JsArray(_)  => name -> new EnumType(json.convertTo[IndexedSeq[String]])
         case _ => throw DeserializationException("Expected an enum definition")
      }
   }

   private def readField(customTypes: Map[String, FieldType])(json: JsValue): FieldType = {
      json match {
         case JsString("string") => SimpleString
         case JsString("bigtext") => LongString
         case JsString("date") => SimpleString // TODO! FIME
         case JsString(typename) => customTypes(typename)
         case _ => throw DeserializationException("Expected typename")
      }
   }
}
