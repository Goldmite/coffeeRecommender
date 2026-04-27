<script lang="ts">
	import { enhance } from '$app/forms';
	import Card from '$lib/components/core/Card.svelte';
	import DetailsModal from '$lib/components/core/Modal.svelte';
	import GenerateButton from '$lib/components/core/SubmitButton.svelte';
	import DetailsModalContent from '$lib/components/DetailsModalContent.svelte';
	import RecommendationList from '$lib/components/RecommendationList.svelte';
	import { m } from '$lib/paraglide/messages.js';
	import type { RecommendationDto } from '$lib/types/recommendation';

	let { form } = $props();

	let loading = $state(false);

	let modalRef: ReturnType<typeof DetailsModal>;
	let selectedCoffee = $state<RecommendationDto | null>(null);

	function handleShowDetails(rec: RecommendationDto) {
		selectedCoffee = rec;
		modalRef.open();
	}
</script>

<form
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
	<GenerateButton disabled={loading}>
		{#if loading}
			<span class="icon-[svg-spinners--180-ring]"></span>
		{:else}
			{m.get_recommendations()}
		{/if}</GenerateButton
	>
</form>
<Card>
	<RecommendationList recommendations={form?.recommendations} onShowDetails={handleShowDetails}
	></RecommendationList>
</Card>

<DetailsModal bind:this={modalRef} headerTxt={selectedCoffee?.coffee.name}>
	{#if selectedCoffee}
		<DetailsModalContent details={selectedCoffee}></DetailsModalContent>
	{/if}
</DetailsModal>
