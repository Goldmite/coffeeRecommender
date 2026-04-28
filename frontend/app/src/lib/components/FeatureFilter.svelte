<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import { slide } from 'svelte/transition';
	import Card from './core/Card.svelte';
	import CheckInput from './core/CheckInput.svelte';
	import ProgressSlider from './core/ProgressSlider.svelte';
	import { flavorCategoryMap, levelToRoast, roastMap } from '$lib/utils/mapping';

	let isFilterOpen = $state(false);

	const DEFAULT_FILTERS = {
		roast: 3,
		sca: 80,
		acidity: 5,
		body: 5,
		aftertaste: 5,
		sweetness: 5,
		bitterness: 5,
	};
	let selectedOrigin: number | null = $state(null);
	let variableFilters = $state({ ...DEFAULT_FILTERS });
	let selectedFlavors = $state<string[]>([]);

	function toLabel(index: number, key: string) {
		if (key == 'roast') {
			return roastMap[levelToRoast[index]];
		} else {
			return index;
		}
	}

	const FLAVOR_CATEGORIES = [
		'FRUITY',
		'FLORAL',
		'SWEET',
		'NUTTY_COCOA',
		'SPICES',
		'SOUR',
		'VEGETAL',
	];
	function toggleFilters() {
		isFilterOpen = !isFilterOpen;
		if (!isFilterOpen) {
			resetFilters(); // reset filters on filter component close
		}
	}
	function resetFilters() {
		variableFilters = { ...DEFAULT_FILTERS };
		selectedFlavors = [];
		selectedOrigin = null;
	}
</script>

{#snippet slidingFilter(labelText: string, target: any, key: string, max: number, min: number)}
	<div>
		<label for={key}>{labelText}:</label><span class="ml-2 italic">{toLabel(target[key], key)}</span
		>
		<ProgressSlider formId="rec-form" name={key} bind:value={target[key]} {max} {min}
		></ProgressSlider>
	</div>
{/snippet}

<Card>
	<div class="bg-secondary/10 px-4 py-2">
		<div class="flex items-center justify-between">
			<div class="flex flex-row items-center gap-1">
				<h3>{m.filters()}</h3>
				<span class="icon-[streamline--filter-2-solid] bg-main-mid"></span>
			</div>
			<div class="flex flex-row items-center gap-4">
				{#if isFilterOpen}
					<button class="size-5" title={m.reset()} onclick={resetFilters}
						><span class="icon-[carbon--filter-reset] size-5 bg-main-mid"></span></button
					>
				{/if}
				<button
					onclick={toggleFilters}
					class="btn-wide bean-border border border-main-border font-semibold
                tracking-wide text-light transition-colors hover:inset-shadow-md {isFilterOpen
						? 'bg-error'
						: 'bg-secondary'}"
				>
					{isFilterOpen ? m.close() : m.open()}
				</button>
			</div>
		</div>
		{#if isFilterOpen}
			<div class="grid grid-cols-2 gap-4 sm:grid-cols-3" transition:slide>
				<div class="col-span-1 grid grid-cols-1 gap-3 sm:col-span-2 sm:grid-cols-2">
					{@render slidingFilter(m.roast(), variableFilters, 'roast', 5, 1)}
					{@render slidingFilter(m.body(), variableFilters, 'body', 10, 1)}
					{@render slidingFilter('SCA', variableFilters, 'sca', 100, 80)}
					{@render slidingFilter(m.aftertaste(), variableFilters, 'aftertaste', 10, 1)}
					{@render slidingFilter(m.acidity(), variableFilters, 'acidity', 10, 1)}
					{@render slidingFilter(m.sweetness(), variableFilters, 'sweetness', 10, 1)}
					<div>
						<label for="origin">{m.origin_type()}</label>
						<div class="grid grid-cols-2 gap-2">
							<CheckInput
								idLabel="single-origin"
								name="origin"
								value={2}
								labelText={m.single_origin()}
								colorScheme="secondary"
								isChecked={selectedOrigin === 2}
								onchange={() => (selectedOrigin = 2)}
							/>

							<CheckInput
								idLabel="blend"
								name="origin"
								value={0}
								labelText={m.blend_origin()}
								colorScheme="secondary"
								isChecked={selectedOrigin === 0}
								onchange={() => (selectedOrigin = 0)}
							/>
						</div>
					</div>
					{@render slidingFilter(m.bitterness(), variableFilters, 'bitterness', 10, 1)}
				</div>

				<div>
					<label for="flavors">{m.flavor_notes()}</label>
					<div class="grid grid-cols-1 gap-2 sm:grid-cols-2">
						{#each FLAVOR_CATEGORIES as flavor}
							<CheckInput
								formId="rec-form"
								idLabel={flavor}
								name="flavors"
								value={flavor}
								isChecked={selectedFlavors.includes(flavor)}
								labelText={flavorCategoryMap[flavor]}
								colorScheme="secondary"
								onchange={(checked: any) => {
									if (checked) {
										selectedFlavors = [...selectedFlavors, flavor];
									} else {
										selectedFlavors = selectedFlavors.filter((f) => f !== flavor);
									}
								}}
							/>
						{/each}
					</div>
				</div>
			</div>
		{/if}
	</div>
</Card>

<style>
	label {
		color: var(--color-secondary);
		font-weight: bold;
	}
</style>
