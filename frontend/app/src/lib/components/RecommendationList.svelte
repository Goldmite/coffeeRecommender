<script lang="ts">
	import { m } from '$lib/paraglide/messages.js';
	import type { RecommendationDto, Recommendations } from '$lib/types/recommendation';
	import RecommendationRow from './RecommendationRow.svelte';

	let { recommendations }: { recommendations: Recommendations | undefined } = $props();

	let selectedDetails = $state<RecommendationDto | null>(null);

	function handleShowDetails(rec: RecommendationDto) {
		selectedDetails = rec;
	}
</script>

<table>
	<thead>
		<tr class="headers">
			<th>{m.coffee_name()}</th>
			<th>{m.roast()}</th>
			<th>{m.origins()}</th>
			<th>{m.process()}</th>
			<th>{m.flavor_notes()}</th>
			<th>{m.accuracy()}</th>
			<th>{m.actions()}</th>
		</tr>
	</thead>
	<tbody>
		{#each recommendations as rec, i}
			<RecommendationRow {rec} rowNr={i} onShowDetails={handleShowDetails} />
		{:else}
			<tr
				><td colspan="7" class="text-center italic p-10 text-lg bg-main-mid/10">
					{m.no_recommendations_generated()}...
				</td>
			</tr>
		{/each}
	</tbody>
</table>

<style>
	table {
		width: 100%;
		border-collapse: collapse;
	}
	.headers {
		background-color: var(--color-main-mid);
		border-bottom: 1px solid var(--color-main-border);
	}
	th {
		padding: 8px;
	}
</style>
