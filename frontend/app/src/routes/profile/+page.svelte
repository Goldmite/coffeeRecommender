<script lang="ts">
	import Card from '$lib/components/core/Card.svelte';
	import { m, user_email } from '$lib/paraglide/messages.js';

	let { data } = $props();
</script>

{#snippet profileInfo(key: string, value: string | undefined)}
	<dt>{key}:</dt>
	<dd>{value ?? '—'}</dd>
{/snippet}
<div class="flex flex-1 justify-center">
	<div class="h-fit w-100">
		<Card>
			<div class="flex flex-col gap-4 p-4">
				<h2 class="border-b border-main-border">{m.profile()}</h2>
				<dl class="grid grid-cols-2 gap-3">
					{@render profileInfo(m.user_name(), data.user?.name)}
					{@render profileInfo(m.user_email(), data.user?.email)}
				</dl>
			</div>
		</Card>
	</div>
</div>
