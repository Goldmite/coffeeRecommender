<script lang="ts">
	import Card from '../core/Card.svelte';

	let { children } = $props();
</script>

<Card>
	<div class="min-w-[300px] p-6">
		{@render children()}
	</div>
</Card>
