import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vitest/config';
import { paraglideVitePlugin } from '@inlang/paraglide-js';

export default defineConfig({
	plugins: [
		paraglideVitePlugin({
			project: './project.inlang',
			outdir: './src/lib/paraglide',
			strategy: ['url', 'cookie', 'baseLocale'],
		}),
		tailwindcss(),
		sveltekit(),
	],
	test: {
		globals: true,
		environment: 'jsdom',
		setupFiles: './src/test-setup.ts',
	},
	resolve: process.env.VITEST
		? {
				conditions: ['browser'],
			}
		: undefined,
});
