package com.radeusgd.archivum.querying

import com.radeusgd.archivum.datamodel.{DMStruct, DMValue}

case class ResultSet(rows: Seq[MultipleResultRow]) {
   def getResult: Seq[ResultRow] = rows.map(_.prefix)

   def filter(predicate: DMValue => Boolean): ResultSet =
      ResultSet(rows.map(_.filter(predicate)))

   /*
   See MultipleResultRow for description.
    */
   def unpackArray(path: String): ResultSet =
      ResultSet(rows.map(_.unpackArray(path)))


   def flatten(): ResultSet = {
      def unpackRow(row: MultipleResultRow): Seq[MultipleResultRow] =
         row.objects.map(obj => MultipleResultRow(row.prefix, Seq(obj)))
      ResultSet(rows.flatMap(unpackRow))
   }

   def flatMap(f: MultipleResultRow => Seq[MultipleResultRow]): ResultSet = {
      ResultSet(rows.flatMap(f))
   }

   /*
   Groups results contained in each of MultipleResultRows into separate, smaller MultipleResultRows.
   If columnName is specified, value that has been grouped by is
    */
   def groupBy[A](path: String, appendPrefix: Option[AppendPrefix], sortBy: DMValue => A)(implicit ord: Ordering[A]): ResultSet = {
      flatMap(_.groupBy(path, appendPrefix, sortBy))
   }

   def aggregate(aggregations: Seq[(String, Seq[DMValue] => DMValue)]): Seq[ResultRow] = {
      rows.map(_.aggregate(aggregations))
   }
}
