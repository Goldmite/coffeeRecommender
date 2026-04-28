<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { ShopResponse } from '$lib/types/recommendation';

	let { shops, lastSelectedIds = [] }: { shops: ShopResponse[]; lastSelectedIds: number[] } =
		$props();
</script>

<div class="grid grid-cols-1 gap-2">
	<h3>{m.shops()}</h3>
	{#each shops as shop (shop.id)}
		<label
			for="shop-{shop.id}"
			class="bean-border flex cursor-pointer gap-1 border border-main-border p-2
        hover:bg-main-border/20 has-checked:border-secondary has-checked:bg-secondary/10"
		>
			<input
				id="shop-{shop.id}"
				name="shopIds"
				value={shop.id}
				type="checkbox"
				class="hidden"
				checked={lastSelectedIds.includes(shop.id)}
			/>

			<span>{shop.name}</span>
		</label>
	{/each}
</div>
