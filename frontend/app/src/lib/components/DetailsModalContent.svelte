<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import { processMap, roastMap } from '$lib/utils/mapping';
	import Progress from './core/Progress.svelte';

	let { details } = $props();

	const altitudeDisplay = $derived(
		details.coffee.altitude[0] == details.coffee.altitude[1]
			? details.coffee.altitude[0]
			: details.coffee.altitude.join('-'),
	);
</script>

{#snippet detailRow(key: string, value: string, extra?: string)}
	<dt>{key}</dt>
	<dd>{value ?? '—'}{extra}</dd>
{/snippet}

{#snippet levelRow(key: string, value: number)}
	<dt>{key}</dt>
	<dd><Progress {value} max={10}></Progress></dd>
{/snippet}

<dl class="mb-5 grid grid-cols-2 gap-3">
	{@render detailRow(m.origins(), details.coffee.origins.join(', '))}
	{@render detailRow(m.process(), processMap[details.coffee.process])}
	{@render detailRow(m.roast(), roastMap[details.coffee.roastLevel])}
	{@render detailRow(m.altitude(), altitudeDisplay, ' m')}
	{@render detailRow('SCA', details.coffee.scaScore)}
	{@render detailRow(m.flavor_notes(), details.coffee.flavorNotes.join(', '))}
	{@render levelRow(m.acidity(), details.coffee.acididty)}
	{@render levelRow(m.body(), details.coffee.body)}
	{@render levelRow(m.aftertaste(), details.coffee.aftertaste)}
	{@render levelRow(m.sweetness(), details.coffee.sweetness)}
	{@render levelRow(m.bitterness(), details.coffee.bitterness)}
</dl>

<section class="mb-5">
	<h4 class="key">{m.description()}</h4>
	<p>{details.coffee.description}</p>
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
