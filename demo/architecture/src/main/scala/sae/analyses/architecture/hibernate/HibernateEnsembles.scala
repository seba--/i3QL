package sae.analyses.architecture.hibernate

import unisson.model.impl.{Concern, Ensemble}
import de.tud.cs.st.vespucci.interfaces.{IEnsemble, IConstraint, IArchitectureModel}
import java.util

/**
 * 
 * @author Ralf Mitschke
 *
 */
object HibernateEnsembles {


    object Hibernate_3_6_6 extends IArchitectureModel {

        val ensembles = new util.HashSet[IEnsemble]()

        val Actions = Ensemble("Actions", "package('org.hibernate.action')\nor class_with_members('org.hibernate.engine','ActionQueue')")
        val Cache = Ensemble("Cache","(\n\tpackage('org.hibernate.cache')\n\twithout\n\t(\n\tclass_with_members('org.hibernate.cache','CacheException')\n\tor \tclass_with_members('org.hibernate.cache','NoCachingEnabledException')\n\t)\n)\nor package('org.hibernate.cache.entry')\n\n or package('org.hibernate.cache.access')\n or package('org.hibernate.cache.entry')\n or package('org.hibernate.cache.impl.bridge')\n or package('org.hibernate.cache.impl')\nor class_with_members('org.hibernate.annotations','CacheConcurrencyStrategy')")
        val Configuration = Ensemble("Configuration", "(package('org.hibernate.cfg')\nwithout\n(\n\tclass_with_members('org.hibernate.cfg','Environment') \n\tor class_with_members('org.hibernate.cfg','Settings')\n\tor class_with_members('org.hibernate.cfg','SettingsFactory')\n\tor class_with_members('org.hibernate.cfg','RecoverableException')\n\tor class_with_members('org.hibernate.cfg','NotYetImplementedException')\n\t or class_with_members('org.hibernate.cfg','Mappings')\n\t or class_with_members('org.hibernate.cfg','ObjectNameNormalizer')\n)\n) or package('org.hibernate.cfg.search')\n or package('org.hibernate.cfg.beanvalidation')\n or package('org.hibernate.cfg.annotations')\n or package('org.hibernate.cfg.annotations.reflection')")
        val DataTypes = Ensemble("DataTypes", "(\n\tpackage('org.hibernate.type')\n\twithout\n\t(\n\t\tclass_with_members('org.hibernate.type','SerializationException')\n\t)\n)\nor package('org.hibernate.usertype') \nor class_with_members('org.hibernate.engine','TypedValue')\n or package('org.hibernate.type.descriptor')\n or package('org.hibernate.type.descriptor.sql')\n or package('org.hibernate.type.descriptor.java')\n or class_with_members('org.hibernate.impl','TypeLocatorImpl')")
        val HibernateORMapping = Ensemble("HibernateORMapping", "package('org.hibernate.mapping')\n or class_with_members('org.hibernate.engine','Mapping')\n or class_with_members('org.hibernate.cfg','Mappings')")
        val HQL = Ensemble("HQL","(\n\tpackage('org.hibernate.hql')\n\twithout \n\t(\n\t\tclass_with_members('org.hibernate.hql','QueryExecutionRequestException')\n\tor class_with_members('org.hibernate.hql','HolderInstantiator')\n\t)\n)\nor \n(\n\tpackage('org.hibernate.hql.ast')\n\twithout \n\t(\n\t\tclass_with_members('org.hibernate.hql.ast','QuerySyntaxException')\n\t\tor class_with_members('org.hibernate.hql.ast','InvalidWithClauseException')\n\t)\n)\n or package('org.hibernate.hql.antlr')\n or package('org.hibernate.loader.hql')\n or package('org.hibernate.hql.ast.exec')\n or package('org.hibernate.hql.ast.util')\n or package('org.hibernate.hql.ast.tree')\n or package('org.hibernate.param')\n or class_with_members('org.hibernate.engine','ParameterBinder')\n or class_with_members('org.hibernate.engine.query','HQLQueryPlan')")
        val IdentifierGenerators = Ensemble("IdentifierGenerators", "(package('org.hibernate.id')\nwithout\n(\n class_with_members('org.hibernate.id','IdentifierGenerationException')\n)\n) or package('org.hibernate.id.enhanced')\n or package('org.hibernate.id.uuid')\n or package('org.hibernate.id.insert')\n or package('org.hibernate.id.factory')")
        val PersistenceManager = Ensemble("PersistenceManager", "package('org.hibernate.collection') or\npackage('org.hibernate.persister') or \npackage('org.hibernate.persister.collection') or\npackage('org.hibernate.persister.entity') or\npackage('org.hibernate.loader.entity') or\npackage('org.hibernate.loader.collection') or\n(\n\tpackage('org.hibernate.loader') \n\twithout class_with_members('org.hibernate.loader','MultipleBagFetchException')\n)\n or class_with_members('org.hibernate.impl','QueryImpl')\n or class_with_members('org.hibernate.impl','ScrollableResultsImpl')\n or class_with_members('org.hibernate.impl','IteratorImpl')\n or class_with_members('org.hibernate.impl','FilterImpl')\n or class_with_members('org.hibernate.impl','AbstractQueryImpl')\n or class_with_members('org.hibernate.engine','HibernateIterator')\n or class_with_members('org.hibernate.engine','ForeignKeys')\n or class_with_members('org.hibernate.event.def','OnUpdateVisitor')\n or class_with_members('org.hibernate.event.def','WrapVisitor')\n or class_with_members('org.hibernate.event.def','ProxyVisitor')\n or class_with_members('org.hibernate.event.def','AbstractVisitor')\n or class_with_members('org.hibernate.event.def','ReattachVisitor')\n or class_with_members('org.hibernate.event.def','DirtyCollectionSearchVisitor')\n or class_with_members('org.hibernate.event.def','EvictVisitor')\n or class_with_members('org.hibernate.event.def','OnReplicateVisitor')\n or class_with_members('org.hibernate.event.def','OnLockVisitor')\n or class_with_members('org.hibernate.event.def','FlushVisitor')\n or class_with_members('org.hibernate.impl','CollectionFilterImpl')\n or class_with_members('org.hibernate.engine','BatchFetchQueue')\n or class_with_members('org.hibernate.engine','CollectionEntry')\n or class_with_members('org.hibernate.engine','CollectionKey')\n or class_with_members('org.hibernate.engine','Collections')\n or class_with_members('org.hibernate.engine','EntityEntry')\n or class_with_members('org.hibernate.engine','EntityKey')\n or class_with_members('org.hibernate.engine','EntityUniqueKey')\n or class_with_members('org.hibernate.engine','PersistenceContext')\n or class_with_members('org.hibernate.engine','SubselectFetch')\n or class_with_members('org.hibernate.engine','TwoPhaseLoad')\n or class_with_members('org.hibernate.engine','QueryParameters')\n or class_with_members('org.hibernate.engine','JoinSequence')\n or class_with_members('org.hibernate.engine','JoinHelper')\n or package('org.hibernate.engine.loading')\n or class_with_members('org.hibernate.engine','CascadeStyle')\n or class_with_members('org.hibernate.engine','CascadingAction')\n or class_with_members('org.hibernate.engine','StatefulPersistenceContext')\n or class_with_members('org.hibernate.impl','NonFlushedChangesImpl')\n or class_with_members('org.hibernate.impl','FetchingScrollableResultsImpl')\n or class_with_members('org.hibernate.impl','AbstractScrollableResults')\n or class_with_members('org.hibernate.engine','Cascade')\n or class_with_members('org.hibernate.engine','NonFlushedChanges')")
        val PropertySettings = Ensemble("PropertySettings", "class_with_members('org.hibernate.cfg','Environment') \nor class_with_members('org.hibernate.cfg','Settings')\nor class_with_members('org.hibernate.cfg','SettingsFactory')")
        val Proxies = Ensemble("Proxies", "package('org.hibernate.proxy')\n or package('org.hibernate.proxy.pojo')\n or package('org.hibernate.proxy.pojo.cglib')\n or package('org.hibernate.proxy.map')\n or package('org.hibernate.proxy.pojo.javassist')\n or package('org.hibernate.proxy.dom4j')")
        val SchemaTool = Ensemble("SchemaTool", "class_with_members('org.hibernate.tool.hbm2ddl','SchemaExportTask')\nor class_with_members('org.hibernate.tool.hbm2ddl','SchemaUpdateTask') \n or class_with_members('org.hibernate.tool.hbm2ddl','SchemaValidatorTask')")
        val SessionManagement = Ensemble("SessionManagement", "class_with_members('org.hibernate.impl','SessionImpl')\n or class_with_members('org.hibernate.impl','SessionFactoryImpl')\n or class_with_members('org.hibernate.impl','SessionFactoryObjectFactory')\n or class_with_members('org.hibernate','Session')\n or class_with_members('org.hibernate','SessionFactory')\n or class_with_members('org.hibernate.engine','SessionFactoryImplementor')\n or class_with_members('org.hibernate.engine','SessionImplementor')\nor class_with_members('org.hibernate.jdbc','JDBCContext') or package('org.hibernate.context')\n or class_with_members('org.hibernate.impl','StatelessSessionImpl')\n or class_with_members('org.hibernate.impl','AbstractSessionImpl')")
        val SQLDialects = Ensemble("SQLDialects", "package('org.hibernate.dialect') \nor\n(\n\tpackage('org.hibernate.sql')\n\twithout\n\t(\n\tclass_with_members('org.hibernate.sql', 'Insert')\n\tor class_with_members('org.hibernate.sql', 'Delete')\n\tor class_with_members('org.hibernate.sql', 'Select')\n\tor class_with_members('org.hibernate.sql', 'Update')\n\t)\n)\nor package('org.hibernate.dialect.function')\n or package('org.hibernate.sql.ordering.antlr')\n or package('org.hibernate.dialect.lock')\n or package('org.hibernate.dialect.resolver')")
        val Transactions = Ensemble("Transactions", "package('org.hibernate.transaction')\nor class_with_members('org.hibernate','Transaction') \nor class_with_members('org.hibernate.engine','TransactionHelper')\n or package('org.hibernate.transaction.synchronization')\n or\n( package('org.hibernate.engine.transaction')\n\twithout class_with_members('org.hibernate.engine.transaction','NullSynchronizationException')\n)")
        val UserAPI = Ensemble("UserAPI", "package('org.hibernate')\nwithout \n(\n\tclass_with_members('org.hibernate','Session') or\n\tclass_with_members('org.hibernate','SessionFactory') or\n\tclass_with_members('org.hibernate','HibernateException') or \n\tclass_with_members(transitive(supertype(class('org.hibernate','HibernateException')))) or\n    class_with_members('org.hibernate','AssertionFailure') or\n\tclass_with_members('org.hibernate','Transaction') or\n    class_with_members('org.hibernate','LazyInitializationException')\n or class_with_members('org.hibernate','EntityMode')\n or class_with_members('org.hibernate','FetchMode')\n or class_with_members('org.hibernate','FlushMode')\n or class_with_members('org.hibernate','LockMode')\n or class_with_members('org.hibernate','ConnectionReleaseMode')\n)")
        val UtilitiesAndExceptions = Ensemble("UtilitiesAndExceptions", "class_with_members('org.hibernate','HibernateException')\nor \n( \n\tclass_with_members(transitive(supertype(class('org.hibernate','HibernateException'))))\n\twithout class_with_members('org.hibernate.classic','ValidationFailure')\n)\nor class_with_members('org.hibernate','AssertionFailure')\nor class_with_members('org.hibernate','LazyInitializationException')\nor package('org.hibernate.util')\nor class_with_members('org.hibernate.engine','RowSelection')\n or class_with_members('org.hibernate.exception','JDBCExceptionHelper')\n or class_with_members('org.hibernate.exception','SQLExceptionConverter')\n or class_with_members('org.hibernate.exception','SQLExceptionConverterFactory')\n or class_with_members('org.hibernate.exception','TemplatedViolatedConstraintNameExtracter')\n or class_with_members('org.hibernate.exception','SQLStateConverter')\n or class_with_members('org.hibernate.exception','Configurable')\n or class_with_members('org.hibernate.exception','ViolatedConstraintNameExtracter') or class_with_members('org.hibernate.engine','Status')\n or class_with_members('org.hibernate','AnnotationException')\n or package('org.hibernate.util.xml')\n or class_with_members('org.hibernate.engine','ValueInclusion')\n or class_with_members('org.hibernate.engine','AssociationKey')\n or class_with_members('org.hibernate.exception','CacheSQLStateConverter')\n or class_with_members('org.hibernate.engine','ExecuteUpdateResultCheckStyle')\n or class_with_members('org.hibernate','EntityMode')\n or class_with_members('org.hibernate','FetchMode')\n or class_with_members('org.hibernate','FlushMode')\n or class_with_members('org.hibernate','LockMode')\n or class_with_members('org.hibernate','ConnectionReleaseMode')\n\t or class_with_members('org.hibernate.cfg','ObjectNameNormalizer')")

        ensembles.add(Actions)
        ensembles.add(Cache)
        ensembles.add(Configuration)
        ensembles.add(DataTypes)
        ensembles.add(HibernateORMapping)
        ensembles.add(HQL)
        ensembles.add(IdentifierGenerators)
        ensembles.add(PersistenceManager)
        ensembles.add(PropertySettings)
        ensembles.add(Proxies)
        ensembles.add(SchemaTool)
        ensembles.add(SessionManagement)
        ensembles.add(SQLDialects)
        ensembles.add(Transactions)
        ensembles.add(UserAPI)
        ensembles.add(UtilitiesAndExceptions)

        val Annotations = Ensemble("Annotations", "package('org.hibernate.annotations')\nwithout\n(\n\tclass_with_members('org.hibernate.annotations','CacheConcurrencyStrategy')\n)")
        val BytecodeInstrumentation = Ensemble("BytecodeInstrumentation", "package('org.hibernate.bytecode.javassist')\n or package('org.hibernate.bytecode.buildtime')\n or package('org.hibernate.bytecode.cglib')\n or package('org.hibernate.bytecode')\n or package('org.hibernate.bytecode.util')")
        val BytecodeInstrumentationTool = Ensemble("BytecodeInstrumentationTool", "package('org.hibernate.tool.instrument.cglib')\nor package('org.hibernate.tool.instrument.javassist')\nor package('org.hibernate.tool.instrument')")
        val BytecodeInterception = Ensemble("BytecodeInterception", "package('org.hibernate.intercept')\n\n or package('org.hibernate.intercept.cglib')\n or package('org.hibernate.intercept.javassist')")
        val ConnectionProvider = Ensemble("ConnectionProvider", "package('org.hibernate.connection')")
        val CriteriaExpressionFramework = Ensemble("CriteriaExpressionFramework", "package('org.hibernate.criterion') \n")
        val DeprecatedUtility = Ensemble("DeprecatedUtility", "class_with_members('org.hibernate.lob','ReaderInputStream')\n or class_with_members('org.hibernate.engine.jdbc','ReaderInputStream')")
        val EntityRepresentation = Ensemble("EntityRepresentation", "package('org.hibernate.tuple')\n or package('org.hibernate.tuple.entity')\n or package('org.hibernate.tuple.component')")
        val EventFramework = Ensemble("EventFramework", "package('org.hibernate.event') \nor\n(\n\tpackage('org.hibernate.event.def') \n\twithout\n\t(\n    class_with_members('org.hibernate.event.def','OnUpdateVisitor')\n or class_with_members('org.hibernate.event.def','WrapVisitor')\n or class_with_members('org.hibernate.event.def','ProxyVisitor')\n or class_with_members('org.hibernate.event.def','AbstractVisitor')\n or class_with_members('org.hibernate.event.def','ReattachVisitor')\n or class_with_members('org.hibernate.event.def','DirtyCollectionSearchVisitor')\n or class_with_members('org.hibernate.event.def','EvictVisitor')\n or class_with_members('org.hibernate.event.def','OnReplicateVisitor')\n or class_with_members('org.hibernate.event.def','OnLockVisitor')\n or class_with_members('org.hibernate.event.def','FlushVisitor')\n\t)\n)")
        val HandwrittenSQLLoader = Ensemble("HandwrittenSQLLoader", "package('org.hibernate.loader.custom.sql')\n or package('org.hibernate.loader.custom')")
        val HibernateClassic = Ensemble("HibernateClassic", "package('org.hibernate.classic')")
        val HibernateCriteria = Ensemble("HibernateCriteria", "class_with_members('org.hibernate','Criteria')\nor class_with_members('org.hibernate.impl','CriteriaImpl')\nor package('org.hibernate.loader.criteria')")
        val HibernateUtilities = Ensemble("HibernateUtilities", "    class_with_members('org.hibernate.engine','Versioning')\n or class_with_members('org.hibernate.engine','FilterDefinition')\n or class_with_members('org.hibernate.engine','NamedSQLQueryDefinition')\n or class_with_members('org.hibernate.engine','NamedQueryDefinition')\n or class_with_members('org.hibernate.engine','Nullability')\n or class_with_members('org.hibernate.engine','UnsavedValueFactory')\n or class_with_members('org.hibernate.engine','VersionValue')\n or class_with_members('org.hibernate.engine','IdentifierValue')\n or class_with_members('org.hibernate.engine','LoadQueryInfluencers')\n or package('org.hibernate.pretty')\n or class_with_members('org.hibernate.cfg','ObjectNameNormalizer')\n or class_with_members('org.hibernate.util','ExternalSessionFactoryConfig')")
        val HQL_Legacy = Ensemble("HQL-Legacy", "package('org.hibernate.hql.classic')")
        val JDBCStatementProcessing = Ensemble("JDBCStatementProcessing", "(package('org.hibernate.jdbc')\nwithout \n(\n\tclass_with_members('org.hibernate.jdbc','JDBCContext')\n\tor class_with_members('org.hibernate.jdbc','BatchedTooManyRowsAffectedException')\n\tor class_with_members('org.hibernate.jdbc','BatchFailedException')\n\tor class_with_members('org.hibernate.jdbc','TooManyRowsAffectedException')\n)\n\n) \nor \n(\n\tpackage('org.hibernate.engine.jdbc')\n\twithout class_with_members('org.hibernate.engine.jdbc','ReaderInputStream')\n)\n or package('org.hibernate.jdbc.util')")
        val JMX = Ensemble("JMX", "package('org.hibernate.jmx')")
        val Metadata = Ensemble("Metadata", "package('org.hibernate.metadata')")
        val Properties = Ensemble("Properties", "package('org.hibernate.property')")
        val ResultTransfomers = Ensemble("ResultTransfomers", "package('org.hibernate.transform')\nor class_with_members('org.hibernate.hql','HolderInstantiator')")
        val SchemaProcessing = Ensemble("SchemaProcessing", "class_with_members('org.hibernate.tool.hbm2ddl','DatabaseMetadata')\n or class_with_members('org.hibernate.tool.hbm2ddl','ColumnMetadata')\n or class_with_members('org.hibernate.tool.hbm2ddl','SchemaUpdate')\n or class_with_members('org.hibernate.tool.hbm2ddl','IndexMetadata')\n or class_with_members('org.hibernate.tool.hbm2ddl','TableMetadata')\n or class_with_members('org.hibernate.tool.hbm2ddl','SchemaExport')\n or class_with_members('org.hibernate.tool.hbm2ddl','ForeignKeyMetadata')\n or class_with_members('org.hibernate.tool.hbm2ddl','ConnectionHelper')\n or class_with_members('org.hibernate.tool.hbm2ddl','SchemaValidator')\n or class_with_members('org.hibernate.tool.hbm2ddl','SuppliedConnectionProviderConnectionHelper')\n or class_with_members('org.hibernate.tool.hbm2ddl','SuppliedConnectionHelper')\n or class_with_members('org.hibernate.tool.hbm2ddl','ManagedProviderConnectionHelper')")
        val SecurityPolicy = Ensemble("SecurityPolicy", "package('org.hibernate.secure')")
        val SQLStatements = Ensemble("SQLStatements", "class_with_members('org.hibernate.sql', 'Insert')\nor class_with_members('org.hibernate.sql', 'Delete')\nor class_with_members('org.hibernate.sql', 'Select')\nor class_with_members('org.hibernate.sql', 'Update')\nor class_with_members('org.hibernate.impl','SQLQueryImpl')\n or \n(\n\tpackage('org.hibernate.engine.query')\n\twithout class_with_members('org.hibernate.engine.query','HQLQueryPlan')\n)\n or package('org.hibernate.engine.query.sql')\n or class_with_members('org.hibernate.engine','ResultSetMappingDefinition')")
        val Statistics = Ensemble("Statistics", "package('org.hibernate.stat') or package('org.hibernate.engine.profile')")

        ensembles.add(Annotations)
        ensembles.add(BytecodeInstrumentation)
        ensembles.add(BytecodeInstrumentationTool)
        ensembles.add(BytecodeInterception)
        ensembles.add(ConnectionProvider)
        ensembles.add(CriteriaExpressionFramework)
        ensembles.add(DeprecatedUtility)
        ensembles.add(EntityRepresentation)
        ensembles.add(EventFramework)
        ensembles.add(HandwrittenSQLLoader)
        ensembles.add(HibernateClassic)
        ensembles.add(HibernateCriteria)
        ensembles.add(HibernateUtilities)
        ensembles.add(HQL_Legacy)
        ensembles.add(JDBCStatementProcessing)
        ensembles.add(JMX)
        ensembles.add(Metadata)
        ensembles.add(Properties)
        ensembles.add(ResultTransfomers)
        ensembles.add(SchemaProcessing)
        ensembles.add(SecurityPolicy)
        ensembles.add(SQLStatements)
        ensembles.add(Statistics)

        def getEnsembles = ensembles

        def getConstraints = new util.HashSet[IConstraint]()

        def getName = "Hibernate_3_6_6"
    }

    object Hibernate_1_0 extends IArchitectureModel {

        val Actions = Ensemble("Actions", "class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession$Executable')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledDeletion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionDeletes')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionUpdates')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionInserts')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRecreate')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledInsertion')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledCollectionRemove')) \nor class_with_members(class('cirrus.hibernate.impl','ScheduledUpdate'))")
        val SessionManagement = Ensemble("SessionManagement", "class_with_members(class('cirrus.hibernate.impl','SessionFactoryObjectFactory')) \nor class_with_members(class('cirrus.hibernate.impl','SessionImplementor')) \nor class_with_members(class('cirrus.hibernate','SessionFactory')) \nor class_with_members(class('cirrus.hibernate','Session')) \nor class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession')) \nor class_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSessionFactory'))\nwithout ( \n\tclass_with_members(class('cirrus.hibernate.impl','RelationalDatabaseSession$Executable'))  \n)")
        val PersistenceManager = Ensemble("PersistenceManager", "class_with_members(class('cirrus.hibernate.impl','ClassPersister')) \nor class_with_members(class('cirrus.hibernate.type','PersistentObjectType')) \nor class_with_members(class('cirrus.hibernate.type','PersistentEnumType')) \nor class_with_members(class('cirrus.hibernate.impl','PersistenceImplementor'))\nor package('cirrus.hibernate.collections')\nor class_with_members(class('cirrus.hibernate.impl','CollectionPersister')) \nor class_with_members(class('cirrus.hibernate.type','PersistentCollectionType'))\nor class_with_members(class('cirrus.hibernate.impl','PersisterCache'))\nor class_with_members(class('cirrus.hibernate.impl','IdentifierIterator')) \nor class_with_members(class('cirrus.hibernate.impl','ByIDQuery'))")
        val Transactions = Ensemble("Transactions", "package('cirrus.hibernate.transaction') or class_with_members(class('cirrus.hibernate','Transaction'))")
        val IdentifierGenerators = Ensemble("IdentifierGenerators", "package('cirrus.hibernate.id')\nwithout class_with_members('cirrus.hibernate.id', 'IDGenerationException')")
        val Proxies = Ensemble("Proxies", "class_with_members(class('cirrus.hibernate.impl','LazyInitializer')) \nor class_with_members(class('cirrus.hibernate.impl','HibernateProxyHelper')) \nor class_with_members(class('cirrus.hibernate.impl','HibernateProxy'))")
        val SchemaTool = Ensemble("SchemaTool", "class_with_members(class('cirrus.hibernate.tools','SchemaExportGUI')) \nor class_with_members(class('cirrus.hibernate.tools','SchemaExport'))")
        val SQLDialects = Ensemble("SQLDialects", "package('cirrus.hibernate.sql')")
        val Configuration = Ensemble("Configuration", "package('cirrus.hibernate.cfg')  \nor class_with_members('cirrus.hibernate','Datastore')\nor class_with_members(transitive(supertype(class('cirrus.hibernate','Datastore'))))")
        val HibernateORMapping = Ensemble("HibernateORMapping", "package('cirrus.hibernate.map')")
        val PropertySettings = Ensemble("PropertySettings", "class_with_members(class('cirrus.hibernate','Environment'))")
        val EJBSupport = Ensemble("EJBSupport", "package('cirrus.hibernate.ejb')")
        val UserAPI = Ensemble("UserAPI", "package('cirrus.hibernate')\nwithout \n(\n\tclass_with_members('cirrus.hibernate','UserType') or\n\tclass_with_members('cirrus.hibernate','Environment') or\n\tclass_with_members('cirrus.hibernate','Session') or\n\tclass_with_members('cirrus.hibernate','SessionFactory') or\n\tclass_with_members('cirrus.hibernate','HibernateException') or \n\tclass_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) or\n    class_with_members('cirrus.hibernate','AssertionFailure') or\n\tclass_with_members('cirrus.hibernate','Transaction') or\n\tclass_with_members('cirrus.hibernate','Databinder') or\n\tclass_with_members('cirrus.hibernate','Hibernate') or\n    class_with_members('cirrus.hibernate','LazyInitializationException') or \n\tclass_with_members('cirrus.hibernate','Datastore')\n)")
        val DataTypes = Ensemble("DataTypes", "package('cirrus.hibernate.type')\nwithout\n(\n\tclass_with_members(class('cirrus.hibernate.type','PersistentObjectType')) \n\tor class_with_members(class('cirrus.hibernate.type','PersistentEnumType')) \n\tor class_with_members(class('cirrus.hibernate.type','PersistentCollectionType'))\n\tor class_with_members('cirrus.hibernate.type','SerializationException')\n\n)\nor class_with_members(class('cirrus.hibernate','UserType'))")
        val CodeGeneratorTool = Ensemble("CodeGeneratorTool", "package('cirrus.hibernate.tools.codegen') or class_with_members(class('cirrus.hibernate.tools','CodeGenerator')) or class_with_members(class('cirrus.hibernate.tools','ProxyGenerator'))")
        val MappingGeneratorTool = Ensemble("MappingGeneratorTool", "package('cirrus.hibernate.tools.reflect') or class_with_members(class('cirrus.hibernate.tools','MapReflectGUI')) or class_with_members(class('cirrus.hibernate.tools','MapReflectGUI$1')) or class_with_members(class('cirrus.hibernate.tools','MapGenerator'))")
        val Cache = Ensemble("Cache","package('cirrus.hibernate.cache')\nwithout class_with_members('cirrus.hibernate.cache','CacheDeadlockException')")
        val HQL = Ensemble("HQL","package('cirrus.hibernate.query')")
        val DeprecatedLegacy = Ensemble("DeprecatedLegacy","class_with_members('cirrus.hibernate','Persistent') or class_with_members('cirrus.hibernate','PersistentLifecycle')")
        val XMLDatabinder = Ensemble("XMLDatabinder", "class_with_members(class('cirrus.hibernate','Databinder')) or class_with_members(class('cirrus.hibernate.xml','XMLDatabinder'))")
        val UtilitiesAndExceptions = Ensemble("UtilitiesAndExceptions", "package('cirrus.hibernate.helpers') \nor class_with_members(class('cirrus.hibernate','HibernateException')) \nor class_with_members(transitive(supertype(class('cirrus.hibernate','HibernateException')))) \nor class_with_members(class('cirrus.hibernate','AssertionFailure'))\nor class_with_members(class('cirrus.hibernate','LazyInitializationException'))")

        val ensembles = new util.HashSet[IEnsemble]()
        ensembles.add(Actions)
        ensembles.add(SessionManagement)
        ensembles.add(PersistenceManager)
        ensembles.add(Transactions)
        ensembles.add(IdentifierGenerators)
        ensembles.add(Proxies)
        ensembles.add(SchemaTool)
        ensembles.add(SQLDialects)
        ensembles.add(Configuration)
        ensembles.add(HibernateORMapping)
        ensembles.add(PropertySettings)
        ensembles.add(EJBSupport)
        ensembles.add(UserAPI)
        ensembles.add(DataTypes)
        ensembles.add(CodeGeneratorTool)
        ensembles.add(MappingGeneratorTool)
        ensembles.add(Cache)
        ensembles.add(HQL)
        ensembles.add(DeprecatedLegacy)
        ensembles.add(XMLDatabinder)
        ensembles.add(UtilitiesAndExceptions)

        def getEnsembles = ensembles

        def getConstraints = new util.HashSet[IConstraint]()

        def getName = "Hibernate_1_0"
    }
}