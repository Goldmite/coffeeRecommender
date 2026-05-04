<script lang="ts">
	import './layout.css';
	import favicon from '$lib/assets/favicon.svg';
	import { m } from '$lib/paraglide/messages.js';
	import { getLocale, localizeHref, setLocale } from '$lib/paraglide/runtime';
	import SubmitButton from '$lib/components/core/SubmitButton.svelte';
	import { slide } from 'svelte/transition';

	let { data, children } = $props();

	let isDarkTheme = $state(false);

	function toggleTheme() {
		isDarkTheme = document.documentElement.classList.toggle('dark');
		localStorage.setItem('theme', isDarkTheme ? 'dark' : 'light');
	}

	let isDropdownOpen = $state(false);
	function toggleDropdown(event: MouseEvent) {
		event.stopPropagation();
		isDropdownOpen = !isDropdownOpen;
	}
	function closeDropdown() {
		if (isDropdownOpen) isDropdownOpen = false;
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

<svelte:window onclick={closeDropdown} />

<div class="flex min-h-svh flex-col">
	<header
		class="sticky top-0 flex h-16 w-full items-center justify-between bg-primary p-4 text-white-taupe shadow-lg"
	>
		<a href={localizeHref('/home')} class="text-xl font-bold uppercase">
			{m.coffee_recommender_system()}
		</a>
		<div class="flex h-8 justify-items-center gap-4">
			{#if data.user}
				<div class="flex">
					<button type="button" class="toggle-icon" aria-label={m.user()} onclick={toggleDropdown}>
						<div class="flex items-center">
							<span class="icon-[solar--user-bold] bg-main"></span>
						</div>
					</button>
					{#if isDropdownOpen}
						<div
							class="bean-border absolute top-12 right-4 z-50 flex w-42 flex-col gap-1 border border-main-border bg-main p-2 text-main-text"
							transition:slide={{ duration: 200 }}
						>
							<div
								class="w-full truncate border-b border-dashed border-main-border font-semibold text-primary"
							>
								{data.user.name}
							</div>
							<h4 class="hover:text-secondary hover:underline">
								<a href={localizeHref('/profile')}>{m.profile_settings()}</a>
							</h4>

							<form action={localizeHref('/logout')} method="POST">
								<SubmitButton isPrimary={false}>{m.logout()}</SubmitButton>
							</form>
						</div>
					{/if}
				</div>
			{/if}
			<button
				class="toggle-icon"
				type="button"
				aria-label="Toggle dark theme"
				onclick={toggleTheme}
			>
				<div class="flex items-center">
					<span class="icon-[streamline--coffee-bean-solid] bg-main"></span>
				</div></button
			>
			{#if getLocale() === 'lt'}
				<button
					class="toggle-icon"
					type="button"
					aria-label="Toggle language to English"
					onclick={() => setLocale('en')}
					><span class="font-bold text-main">EN</span>
				</button>
			{:else}
				<button
					class="toggle-icon"
					type="button"
					aria-label="Toggle language to Lithuanian"
					onclick={() => setLocale('lt')}
					><span class="font-bold text-main">LT</span>
				</button>
			{/if}
		</div>
	</header>

	<div class="flex flex-1 flex-col p-4">
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
