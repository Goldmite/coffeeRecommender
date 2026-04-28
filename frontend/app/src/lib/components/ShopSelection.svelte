<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { ShopResponse } from '$lib/types/recommendation';
	import CheckInput from './core/CheckInput.svelte';

	let { shops, lastSelectedIds = [] }: { shops: ShopResponse[]; lastSelectedIds: number[] } =
		$props();
</script>

<div class="grid grid-cols-1 gap-2">
	<h3>{m.shops()}</h3>
	{#each shops as shop (shop.id)}
		<CheckInput
			idLabel="shop-{shop.id}"
			name="shopIds"
			value={shop.id}
			isChecked={lastSelectedIds.includes(shop.id)}
			labelText={shop.name}
		/>
	{/each}
</div>
