package sae
package bytecode
package model

case class Method(clazz : ClassFile, name : String, parameters : Seq[de.tud.cs.st.bat.Type], returnType : de.tud.cs.st.bat.Type)