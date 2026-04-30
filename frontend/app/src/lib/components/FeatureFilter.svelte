<script lang="ts">
	import { m } from '$lib/paraglide/messages';
	import { slide } from 'svelte/transition';
	import Card from './core/Card.svelte';
	import CheckInput from './core/CheckInput.svelte';
	import ProgressSlider from './core/ProgressSlider.svelte';
	import { flavorCategoryMap, intensityMap, levelToRoast, roastMap } from '$lib/utils/mapping';

	let isFilterOpen = $state(false);

	const DEFAULT_FILTERS: any = {
		roast: 3,
		sca: 3,
		acidity: 3,
		body: 3,
		aftertaste: 3,
		sweetness: 3,
		bitterness: 3,
	};
	let selectedOrigin: string = $state('');
	let variableFilters = $state({ ...DEFAULT_FILTERS });
	let selectedFlavors = $state<string[]>([]);

	function toLabel(index: number, key: string) {
		if (key == 'roast') {
			return levelToRoast[index];
		} else {
			return intensityMap[index];
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
		selectedOrigin = '';
	}
	const areFiltersUsed = $derived(
		// check if any value in variableFilters differs from DEFAULTS
		Object.keys(variableFilters).some((key) => variableFilters[key] !== DEFAULT_FILTERS[key]) ||
			// check if the flavors array has any items
			selectedFlavors.length > 0 ||
			// check if origin is not an empty string
			selectedOrigin !== '',
	);
	function handleOriginChange(label: string) {
		if (selectedOrigin === label) {
			selectedOrigin = '';
		} else {
			selectedOrigin = label;
		}
	}
</script>

{#snippet slidingFilter(labelText: string, target: any, key: string)}
	<div>
		<label for={key}>{labelText}:</label><span class="ml-2 italic">{toLabel(target[key], key)}</span
		>
		<ProgressSlider formId="rec-form" name={key} bind:value={target[key]}></ProgressSlider>
	</div>
{/snippet}

{#snippet originTypeFilter(label: string, labelText: string)}
	<CheckInput
		formId="rec-form"
		idLabel={label}
		name="origin-type"
		value={label}
		{labelText}
		colorScheme="secondary"
		isChecked={selectedOrigin === label}
		onchange={() => handleOriginChange(label)}
	/>
{/snippet}

<Card>
	<input form="rec-form" type="hidden" name="filterEnabled" value={areFiltersUsed} />
	<div class="bg-secondary/10 px-4 py-2">
		<div class="flex items-center justify-between">
			<div class="flex flex-row items-center gap-1">
				<h3>{m.filters()}</h3>
				<span class="icon-[streamline--filter-2-solid] bg-main-mid"></span>
			</div>
			<div class="flex flex-row items-center gap-4">
				{#if isFilterOpen}
					<button
						class="size-5 disabled:opacity-30"
						title={m.reset()}
						onclick={resetFilters}
						disabled={!areFiltersUsed}
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
					{@render slidingFilter(m.roast(), variableFilters, 'roast')}
					{@render slidingFilter(m.body(), variableFilters, 'body')}
					{@render slidingFilter('SCA', variableFilters, 'sca')}
					{@render slidingFilter(m.aftertaste(), variableFilters, 'aftertaste')}
					{@render slidingFilter(m.acidity(), variableFilters, 'acidity')}
					{@render slidingFilter(m.sweetness(), variableFilters, 'sweetness')}
					<div>
						<label for="origin">{m.origin_type()}</label>
						<div class="grid grid-cols-2 gap-2">
							{@render originTypeFilter('single-origin', m.single_origin())}
							{@render originTypeFilter('blend', m.blend_origin())}
						</div>
					</div>
					{@render slidingFilter(m.bitterness(), variableFilters, 'bitterness')}
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
