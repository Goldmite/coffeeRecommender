<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { m } from '$lib/paraglide/messages.js';
	import { getLocale, setLocale } from '$lib/paraglide/runtime';

	let { children } = $props();

	let isDarkTheme = $state(false);

	function toggleTheme() {
		isDarkTheme = document.documentElement.classList.toggle('dark');
		localStorage.setItem('theme', isDarkTheme ? 'dark' : 'light');
	}
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<script>
		const theme = localStorage.getItem('theme');
		const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;

		if (theme === 'dark' || (!theme && systemDark)) {
			document.documentElement.classList.add('dark');
		} else {
			document.documentElement.classList.remove('dark');
		}
	</script>
</svelte:head>

<div class="flex min-h-svh flex-col">
	<header
		class="sticky top-0 flex h-16 w-full items-center justify-between bg-primary p-4 text-white-taupe shadow-lg"
	>
		<a href="/home" class="text-xl font-bold uppercase"> {m.coffee_recommender_system()} </a>
		<div class="flex h-8 justify-items-center gap-4">
			<button
				class="toggle-icon"
				type="button"
				aria-label="Toggle dark theme"
				onclick={toggleTheme}
			>
				<div class="flex items-center">
					<span class="icon-[streamline--coffee-bean-solid] bg-main"></span>
				</div>
			</button>
			{#if getLocale() === 'lt'}
				<button
					class="toggle-icon font-light"
					type="button"
					aria-label="Toggle language to English"
					onclick={() => setLocale('en')}
					><span>EN</span>
				</button>
			{:else}
				<button
					class="toggle-icon font-light"
					type="button"
					aria-label="Toggle language to Lithuanian"
					onclick={() => setLocale('lt')}
					><span>LT</span>
				</button>
			{/if}
		</div>
	</header>

	<div class="flex flex-1 p-4">
		{@render children()}
	</div>

	<footer></footer>
</div>

<style>
	.toggle-icon {
		height: 100%;
		width: 1.75rem;
		padding: 0.25rem;
	}
	.toggle-icon span {
		width: var(--text-xl);
		height: var(--text-xl);
		font-size: var(--text-sm);
	}
</style>
