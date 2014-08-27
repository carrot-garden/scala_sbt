package sbt
package cablecar

import org.apache.ivy.core
import core.resolve.{ ResolveData => IvyResolveData }
import org.apache.ivy.plugins.resolver.DependencyResolver
import core.module.descriptor.{ ModuleDescriptor, DependencyDescriptor }

import collection.mutable

private[sbt] final class ModuleDescriptorCache {
  import ModuleDescriptorCache._
  val mds: mutable.Map[(String, String, String), ModuleDescriptorCacheEntry] = mutable.Map()
  def loadModuleDescriptor(ivyData: IvyResolveData, resolver: DependencyResolver, dd: DependencyDescriptor): Option[ModuleDescriptorCacheEntry] =
    {
      val mrid = dd.getDependencyRevisionId
      val key = (mrid.getOrganisation, mrid.getName, mrid.getRevision) // IvyRetrieve.toModuleID(requestedRevisionId).copy(configurations = None)
      mds.get(key) orElse {
        val moduleOpt = Option(resolver.getDependency(dd, ivyData))
        moduleOpt map { module =>
          val md = module.getDescriptor
          val resolver = module.getResolver
          val artifactResolver = module.getArtifactResolver
          resolver.getRepositoryCacheManager.saveResolvers(md, resolver.getName, artifactResolver.getName)
          val x = ModuleDescriptorCacheEntry(md, resolver, artifactResolver)
          mds += ((key, x))
          x
        }
      }
    }

}

private[sbt] object ModuleDescriptorCache {
  case class ModuleDescriptorCacheEntry(
    md: ModuleDescriptor,
    resolver: DependencyResolver,
    artifactResolver: DependencyResolver)
}
