// @ts-check
import { defineConfig } from 'astro/config'
import starlight from '@astrojs/starlight'
import starlightAutoImport from './src/plugins/starlight-auto-import'

// https://astro.build/config
export default defineConfig({
    site: 'https://infolektuell.github.io',
    base: '/gradle-jpackage/',
    trailingSlash: 'always',
    integrations: [
        starlight({
            plugins: [starlightAutoImport()],
            title: 'Gradle Jpackage Plugin',
            description:
                'Uses Jpackage to create native installers for Java apps built with gradle and its Application plugin',
            logo: {
                src: './src/assets/logo.svg',
            },
            social: [
                {
                    icon: 'seti:gradle',
                    label: 'Gradle Plugin Portal',
                    href: 'https://plugins.gradle.org/plugin/de.infolektuell.jpackage',
                },
                { icon: 'github', label: 'GitHub', href: 'https://github.com/infolektuell/gradle-jpackage' },
            ],
            editLink: {
                baseUrl: 'https://github.com/infolektuell/gradle-jpackage/edit/main/docs/',
            },
            components: {
                SiteTitle: './src/components/SiteTitle.astro',
            },
            sidebar: [
                {
                    label: 'Introduction',
                    autogenerate: { directory: 'introduction' },
                },
                {
                    label: 'Guides',
                    autogenerate: { directory: 'guides' },
                },
                {
                    label: 'API Docs',
                    link: 'https://infolektuell.github.io/gradle-jpackage/reference/',
                    attrs: { target: '_blank' },
                },
            ],
        }),
    ],
    devToolbar: { enabled: false },
})
