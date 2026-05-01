<script lang="ts">
	import { m } from '$lib/paraglide/messages.js';
	import type { CoffeeBeanResponse, PurchaseDto } from '$lib/types/recommendation';
	import CoffeeRow from './CoffeeRow.svelte';

	let {
		purchases,
		currentPage,
		totalPages,
		onShowDetails,
		onRatingClick,
	}: {
		purchases: PurchaseDto[] | undefined;
		currentPage: number;
		totalPages: number;
		onShowDetails: (coffee: CoffeeBeanResponse) => void;
		onRatingClick: (coffeeId: number) => void;
	} = $props();
</script>

<table>
	<thead>
		<tr class="headers">
			<th>{m.coffee_name()}</th>
			<th>{m.roast()}</th>
			<th>{m.origins()}</th>
			<th>{m.process()}</th>
			<th>{m.shop()}</th>
			<th>{m.rating()}</th>
			<th>{m.actions()}</th>
			<th>{m.date()}</th>
		</tr>
	</thead>
	<tbody>
		{#each purchases as p, i}
			<CoffeeRow
				coffee={p.coffee}
				rating={p.rating}
				purchaseDate={p.purchaseDate}
				rowNr={i}
				{onShowDetails}
				{onRatingClick}
			/>
		{:else}
			<tr
				><td colspan="8" class="text-center italic p-10 text-lg bg-main-mid/10">
					{m.no_purchased_coffees()}...
				</td>
			</tr>
		{/each}
	</tbody>
</table>
{#if totalPages > 1}
	<div class="flex flex-row justify-center gap-4 bg-main-mid py-1 text-sm italic">
		<div class="w-20 text-right">
			<a href="?page={currentPage - 1}" class:invisible={currentPage === 0}>
				{m.previous()}
			</a>
		</div>
		<span>{currentPage + 1} / {totalPages}</span>
		<div class="w-20 text-left">
			<a href="?page={currentPage + 1}" class:invisible={currentPage >= totalPages - 1}>
				{m.next()}
			</a>
		</div>
	</div>
{/if}

<style>
	table {
		width: 100%;
		border-collapse: collapse;
	}
	.headers {
		background-color: var(--color-main-mid);
		border: 1px solid var(--color-main-border);
	}
	th {
		padding: 8px;
	}
</style>
