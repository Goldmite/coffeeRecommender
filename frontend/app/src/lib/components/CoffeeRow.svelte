<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { CoffeeBeanResponse } from '$lib/types/recommendation';
	import { formatEnum, processMap, roastToLevel } from '$lib/utils/mapping';

	let {
		coffee,
		rating,
		rowNr,
		onShowDetails,
		onRatingClick,
	}: {
		coffee: CoffeeBeanResponse;
		rating?: number;
		rowNr: number;
		onShowDetails: (coffee: CoffeeBeanResponse) => void;
		onRatingClick: (coffeeId: number) => void;
	} = $props();
	// fallback to shop url if product url missing
	const prodUrl = $derived(coffee.productUrl || coffee.shop.url);
	const displayProcess = $derived(formatEnum(coffee.process, processMap));
	const roast = $derived(roastToLevel[coffee.roastLevel]);
</script>

<tr class="{rowNr % 2 && 'bg-main-mid/10'} hover:bg-main-mid/20">
	<td>
		{coffee.name}
	</td>
	<td class="whitespace-nowrap">
		{#each [1, 2, 3, 4, 5] as level}
			{#if level <= roast}
				<span class="icon-[streamline--coffee-bean-solid] bg-main-mid"></span>
			{:else}
				<span class="icon-[streamline--coffee-bean] bg-main-mid"></span>
			{/if}
		{/each}
	</td>
	<td>
		{coffee.origins.join(', ')}
	</td>
	<td>
		<span>{displayProcess}</span>
	</td>
	<td>
		{coffee.shop.name}
	</td>
	<td>
		<button onclick={() => onRatingClick(coffee.id)} title={m.rate()} class="rating">
			{#each [1, 2, 3, 4, 5] as ratingLevel}
				{#if rating != undefined && ratingLevel <= rating}
					<span class="icon-[streamline--coffee-bean-solid] bg-attention"></span>
				{:else}
					<span class="icon-[streamline--coffee-bean] bg-attention"></span>
				{/if}
			{/each}
		</button>
	</td>
	<td>
		<div class="flex flex-row items-center justify-between gap-4">
			<a href={prodUrl} target="_blank" class="text-secondary underline">
				{m.view()}
			</a>
			<button onclick={() => onShowDetails(coffee)} title={m.details()} class="details">
				<span class="icon-[streamline--bullet-list-solid]"></span>
			</button>
		</div>
	</td>
</tr>

<style>
	tr {
		border-bottom: 1px solid var(--color-main-border);
	}
	td {
		border: 1px solid var(--color-main-border);
		padding: 0.5rem;
	}
	.rating {
		height: 24px;
		padding: 2px;
	}
</style>
