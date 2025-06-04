package dev.isxander.modstitch.shadow.extensions

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import dev.isxander.modstitch.base.extensions.modstitch
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.kotlin.dsl.*
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo

interface ShadowExtension {
    /**
     * The package to relocate dependencies to.
     * This is the base package that all relocations will be relative to.
     *
     * If you called `dependency("something", "com.example" to "example"),
     * the relocated package would be `$relocatePackage.example`.
     */
    val relocatePackage: Property<String>

    /**
     * Add a dependency to shadow jar, with a map of relocations.
     */
    fun dependency(dependencyNotation: Any, relocations: Map<PackageName, RelocateId>)
    /**
     * Add a dependency to shadow jar, with a map of relocations.
     * `relocations` must not be empty.
     */
    fun dependency(dependencyNotation: Any, vararg relocations: Pair<PackageName, RelocateId>)
    /**
     * Add a dependency to shadow jar, with a map of relocations.
     */
    fun dependency(dependencyNotation: Any, action: Action<MutableMap<PackageName, RelocateId>>)

    /**
     * Add a dependency to shadow jar, with a map of relocations and configuration
     */
    fun dependency(dependencyNotation: Any, relocations: Map<PackageName, RelocateId>, dependencyConfiguration: Action<ExternalModuleDependency>)
}

@Suppress("LeakingThis")
open class ShadowExtensionImpl(
    objects: ObjectFactory,
    @Transient private val target: Project
) : ShadowExtension {
    override val relocatePackage = objects.property<String>()

    override fun dependency(dependencyNotation: Any, relocations: Map<PackageName, RelocateId>) {

        dependency(dependencyNotation, relocations) {}
    }

    override fun dependency(dependencyNotation: Any, action: Action<MutableMap<PackageName, RelocateId>>) {
        dependency(dependencyNotation, mutableMapOf<PackageName, RelocateId>().also(action::execute))
    }

    override fun dependency(
        dependencyNotation: Any,
        relocations: Map<PackageName, RelocateId>,
        dependencyConfiguration: Action<ExternalModuleDependency>
    ) {
        require(relocations.isNotEmpty()) { "At least one relocation must be provided." }
        addDependencyTo(target.dependencies, "modstitchShadow", dependencyNotation, dependencyConfiguration)
        target.tasks.named<ShadowJar>("shadowJar") {
            relocations.forEach { (pkg, id) ->
                relocate(pkg, "${relocatePackage.get()}.$id")
            }
        }
    }

    override fun dependency(dependencyNotation: Any, vararg relocations: Pair<PackageName, RelocateId>) {
        dependency(dependencyNotation, relocations.toMap())
    }
}

typealias PackageName = String
typealias RelocateId = String
