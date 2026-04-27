<script lang="ts">
	import { enhance } from '$app/forms';
	import Card from '$lib/components/core/Card.svelte';
	import DetailsModal from '$lib/components/core/Modal.svelte';
	import GenerateButton from '$lib/components/core/SubmitButton.svelte';
	import DetailsModalContent from '$lib/components/DetailsModalContent.svelte';
	import RecommendationList from '$lib/components/RecommendationList.svelte';
	import TasteOnboarding from '$lib/components/TasteOnboarding.svelte';
	import { m } from '$lib/paraglide/messages.js';
	import type { RecommendationDto } from '$lib/types/recommendation';
	import type { PageProps } from './$types';

	let { data, form }: PageProps = $props();

	let loading = $state(false);
	const count = $derived(form?.recommendations?.length ?? 0);
	const isOnboarded = $derived(form?.success || !data.needsOnboarding);

	let modalRef: ReturnType<typeof DetailsModal>;
	let selectedCoffee = $state<RecommendationDto | null>(null);

	function handleShowDetails(rec: RecommendationDto) {
		selectedCoffee = rec;
		modalRef.open();
	}
</script>

<div class="flex flex-col">
	<h2 class="mt-1 mb-5 ml-2 font-semibold">{m.coffee_beans_for_you()}</h2>
	<div class="flex flex-row gap-7">
		<Card>
			<form
				class="p-4"
				method="POST"
				action="?/fetch"
				use:enhance={() => {
					loading = true;
					return async ({ update }) => {
						await update();
						loading = false;
					};
				}}
			>
				{#if isOnboarded}
					<GenerateButton disabled={loading}>
						{#if loading}
							<span class="icon-[svg-spinners--180-ring]"></span>
						{:else}
							{m.get_recommendations()}
						{/if}</GenerateButton
					>
				{:else}
					<TasteOnboarding></TasteOnboarding>
				{/if}
			</form>
		</Card>
		<div>
			<Card>
				<RecommendationList
					recommendations={form?.recommendations}
					onShowDetails={handleShowDetails}
				></RecommendationList>
			</Card>
			<span class="mt-1 ml-2 text-sm text-main-border italic"
				>{m.recommendation_count({ count })}.</span
			>
		</div>
	</div>
</div>

<DetailsModal bind:this={modalRef} headerTxt={selectedCoffee?.coffee.name}>
	{#if selectedCoffee}
		<DetailsModalContent details={selectedCoffee}></DetailsModalContent>
	{/if}
</DetailsModal>
