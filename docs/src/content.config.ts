import { defineCollection } from 'astro:content'
import { docsLoader } from '@astrojs/starlight/loaders'
import { docsSchema } from '@astrojs/starlight/schema'
import { gradlePluginsLoader } from './lib/gradle'

export const collections = {
    docs: defineCollection({ loader: docsLoader(), schema: docsSchema() }),
    gradlePlugins: defineCollection({ loader: gradlePluginsLoader({ ids: ['de.infolektuell.jpackage'] }) }),
}
