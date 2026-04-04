<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { onMount } from 'svelte';

	let { children } = $props();

	const headerTitle = 'Coffee Recommender System';
	let isDarkTheme = $state(false);

	onMount(() => {
		isDarkTheme = localStorage.getItem('theme') === 'dark';
		updateTheme();
	});

	function toggleTheme() {
		isDarkTheme = !isDarkTheme;
		localStorage.setItem('theme', isDarkTheme ? 'dark' : 'light');
		updateTheme();
	}

	function updateTheme() {
		document.documentElement.classList.toggle('dark', isDarkTheme);
	}
</script>

<svelte:head>
	<link rel="icon" href={favicon} />
	<script>
		const theme = localStorage.getItem('theme');
		if (theme === 'dark') {
			document.documentElement.classList.addEventListener('dark');
		}
	</script>
</svelte:head>

<header
	class="sticky top-0 flex h-16 w-full items-center justify-between bg-secondary p-4 text-white-taupe"
>
	<a href="/" class="text-xl font-bold uppercase"> {headerTitle} </a>
	<div class="flex gap-4">
		<button
			type="button"
			aria-label="Toggle dark theme"
			class="rounded-full p-2"
			onclick={toggleTheme}
		>
			{#if isDarkTheme}
				<span class="bean icon-[streamline--coffee-bean] text-main-mid"></span>
			{:else}
				<span class="bean text-bean-roast icon-[streamline--coffee-bean-solid] text-main-mid"
				></span>
			{/if}
		</button>
	</div>
</header>

<div class="min-h-svh w-full p-4">
	{@render children()}
</div>

<footer></footer>

<style>
	.bean {
		width: var(--text-xl);
		height: var(--text-xl);
	}
</style>
