<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { RecommendationDto } from '$lib/types/recommendation';
	import { formatEnum, processMap, roastToLevel } from '$lib/utils/mapping';

	let {
		rec,
		rowNr,
		onShowDetails,
	}: { rec: RecommendationDto; rowNr: number; onShowDetails: (rec: RecommendationDto) => void } =
		$props();
	// fallback to shop url if product url missing
	const prodUrl = $derived(rec.coffee.productUrl || rec.coffee.shop.url);
	const displayProcess = $derived(formatEnum(rec.coffee.process, processMap));
	const roast = $derived(roastToLevel[rec.coffee.roastLevel]);
</script>

<tr class="{rowNr % 2 && 'bg-main-mid/10'} hover:bg-main-mid/20">
	<td>
		{rec.coffee.name}
	</td>
	<td>
		{#each [1, 2, 3, 4, 5] as level}
			{#if level <= roast}
				<span class="icon-[streamline--coffee-bean-solid] bg-main-mid"></span>
			{:else}
				<span class="icon-[streamline--coffee-bean] bg-main-mid"></span>
			{/if}
		{/each}
	</td>
	<td>
		{rec.coffee.origins.join(', ')}
	</td>
	<td>
		<span class="">{displayProcess}</span>
	</td>
	<td>
		{rec.coffee.flavorNotes.join(', ')}
	</td>
	<td>
		<span>{(rec.score * 100).toFixed(0)}%</span>
	</td>
	<td>
		<div class="flex flex-row items-center justify-between gap-4">
			<a href={prodUrl} target="_blank" class="text-secondary underline">
				{m.view()}
			</a>
			<button onclick={() => onShowDetails(rec)} title={m.details()} class="details">
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
	.details {
		height: 24px;
		padding: 2px;
	}
</style>
