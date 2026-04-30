<script lang="ts">
	import { page } from '$app/state';
	import { m } from '$lib/paraglide/messages';
	import { localizeHref } from '$lib/paraglide/runtime';

	let { children } = $props();

	const navPages = [
		{ path: '/home', label: m.coffee_beans_for_you(), navName: m.recommendations() },
		{ path: '/purchases', label: m.coffee_beans_purchased(), navName: m.purchases() },
	];
	let currentPageTitle = $derived(
		navPages.find((p) => page.url.pathname.includes(p.path))?.label ?? '',
	);
</script>

<div class="flex justify-center">
	<div class="flex w-1/2 items-baseline justify-between">
		<h2 class="mt-1 mb-5 ml-2 w-1/2 font-semibold">{currentPageTitle}</h2>
		<nav class="flex gap-2">
			{#each navPages as tab}
				<a
					href={localizeHref(tab.path)}
					class="px-4 py-1 font-bold transition-colors {page.url.pathname.includes(tab.path)
						? 'text-primary underline'
						: 'hover:text-main-mid'}"
				>
					{tab.navName}
				</a>
			{/each}
		</nav>
	</div>
</div>
<div class="flex justify-center">
	{@render children()}
</div>
