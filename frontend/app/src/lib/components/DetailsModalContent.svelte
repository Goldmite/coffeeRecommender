<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import type { CoffeeBeanResponse } from '$lib/types/recommendation';
	import { processMap, roastMap } from '$lib/utils/mapping';
	import Progress from './core/Progress.svelte';

	let { details }: { details: CoffeeBeanResponse } = $props();

	const altitudeDisplay = $derived(
		details.altitude[0] == details.altitude[1]
			? details.altitude[0].toString()
			: details.altitude.join('-'),
	);
</script>

{#snippet detailRow(key: string, value: string, extra?: string)}
	<dt>{key}</dt>
	<dd>{value ?? '—'}{extra}</dd>
{/snippet}

{#snippet levelRow(key: string, value: number | null)}
	<dt>{key}</dt>
	<dd><Progress {value} max={10}></Progress></dd>
{/snippet}

<dl class="mb-5 grid grid-cols-2 gap-3">
	{@render detailRow(m.origins(), details.origins.join(', '))}
	{@render detailRow(m.process(), processMap[details.process])}
	{@render detailRow(m.roast(), roastMap[details.roastLevel])}
	{@render detailRow(m.altitude(), altitudeDisplay, ' m')}
	{@render detailRow('SCA', details.scaScore?.toString() ?? '')}
	{@render detailRow(m.flavor_notes(), details.flavorNotes.join(', '))}
	{@render levelRow(m.acidity(), details.acidity)}
	{@render levelRow(m.body(), details.body)}
	{@render levelRow(m.aftertaste(), details.aftertaste)}
	{@render levelRow(m.sweetness(), details.sweetness)}
	{@render levelRow(m.bitterness(), details.bitterness)}
</dl>

<section class="mb-5">
	<h4 class="key">{m.description()}</h4>
	<p>{details.description}</p>
</section>

<style>
	dt {
		color: var(--color-secondary);
		font-weight: bold;
		border-bottom: 1px dashed var(--color-secondary);
	}
	dd,
	.key {
		border-bottom: 1px solid var(--color-main-border);
	}
</style>
