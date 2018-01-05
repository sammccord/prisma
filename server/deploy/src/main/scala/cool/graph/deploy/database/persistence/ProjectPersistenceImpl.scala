package cool.graph.deploy.database.persistence

import cool.graph.deploy.database.tables
import cool.graph.deploy.database.tables.{Migration, ProjectTable, Tables}
import cool.graph.shared.models.Project
import slick.jdbc.MySQLProfile.api._
import slick.jdbc.MySQLProfile.backend.DatabaseDef

import scala.concurrent.{ExecutionContext, Future}

case class ProjectPersistenceImpl(
    internalDatabase: DatabaseDef
)(implicit ec: ExecutionContext)
    extends ProjectPersistence {

  override def load(id: String): Future[Option[Project]] = {
    internalDatabase
      .run(ProjectTable.byIdWithMigration(id))
      .map(_.map { projectWithMigration =>
        DbToModelMapper.convert(projectWithMigration._1, projectWithMigration._2)
      })
  }

  override def create(project: Project): Future[Unit] = {
    val addProject = Tables.Projects += ModelToDbMapper.convert(project)
    internalDatabase.run(addProject).map(_ => ())
  }

  override def loadAll(): Future[Seq[Project]] = {
    internalDatabase.run(ProjectTable.loadAllWithMigration()).map(_.map { case (p, m) => DbToModelMapper.convert(p, m) })
  }

  override def update(project: Project): Future[_] = {
    val dbRow = ModelToDbMapper.convert(project)
    internalDatabase.run(Tables.Projects.filter(_.id === project.id).update(dbRow))
  }
}