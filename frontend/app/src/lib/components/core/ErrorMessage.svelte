<script lang="ts">
	import { fly } from 'svelte/transition';

	let { msg, nr = $bindable<number>() } = $props();
</script>

{#key nr}
	<div in:fly={{ y: -80 }} class="error-banner flex items-center">
		<p class="break-all whitespace-normal">{msg}</p>
		{#if nr > 0}
			<span class="ml-auto text-sm">#{nr}</span>
		{/if}
	</div>
{/key}
