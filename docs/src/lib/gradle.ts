import type { Loader } from 'astro/loaders'
import { z } from 'astro/zod'
import { fromHtml } from 'hast-util-from-html'
import { select } from 'hast-util-select'

export function gradlePluginsLoader(options: { ids: string[] }) {
    const selectors = {
        id: '#plugin-id',
        pluginDescription: '#plugin-description',
        website: '#website',
        vcs: '#vcsUrl',
        latestVersion: '#plugin-id-version',
        versionDescription: '#plugin-version-description',
        versionCreatedDate: '.version-created-date',
    }
    const baseUrl = new URL('https://plugins.gradle.org/plugin/')
    return {
        name: 'gradle-plugins-loader',
        load: async ({ store, parseData, logger }) => {
            for (const id of options.ids) {
                const href = new URL(id, baseUrl)
                const response = await fetch(href)
                if (!response.ok) {
                    logger.warn(`Failed to fetch the plugin page for ${id}`)
                    continue
                }
                const html = await response.text()
                const tree = fromHtml(html)
                const descriptionNode = select(selectors.pluginDescription, tree)
                const description = descriptionNode?.children?.find((n) => n.type === 'text')?.value
                const websiteNode = select(selectors.website, tree)
                const website = websiteNode?.children?.find((n) => n.type === 'text')?.value
                const vcsNode = select(selectors.vcs, tree)
                const vcs = vcsNode?.children?.find((n) => n.type === 'text')?.value
                const versionNode = select(selectors.latestVersion, tree)
                const version = versionNode?.children?.find((n) => n.type === 'text')?.value
                const versionCreatedDateNode = select(selectors.versionCreatedDate, tree)
                const versionCreatedDate = versionCreatedDateNode?.children?.find((n) => n.type === 'text')?.value
                const releaseNotesNode = select(selectors.versionDescription, tree)
                const releaseNotes = releaseNotesNode?.children?.find((n) => n.type === 'text')?.value
                const rawData = {
                    id,
                    href: href.href,
                    description,
                    website,
                    vcs,
                    version,
                    releaseNotes,
                    versionCreatedDate,
                }
                try {
                    const data = await parseData({ id, data: rawData })
                    store.set({ id, data })
                } catch (e) {
                    logger.error(e)
                }
            }
        },
        schema: pluginSchema,
    } satisfies Loader
}

export const pluginSchema = z.object({
    id: z.string(),
    href: z.url(),
    description: z.string(),
    website: z.url(),
    vcs: z.url(),
    version: z.string(),
    versionCreatedDate: z.string(),
    releaseNotes: z.string(),
})
