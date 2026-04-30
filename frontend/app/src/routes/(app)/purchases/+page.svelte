<script lang="ts">
	import CoffeePagination from '$lib/components/CoffeePagination.svelte';
	import Card from '$lib/components/core/Card.svelte';
	import Modal from '$lib/components/core/Modal.svelte';
	import DetailsModalContent from '$lib/components/DetailsModalContent.svelte';
	import { m } from '$lib/paraglide/messages';
	import type { CoffeeBeanResponse } from '$lib/types/recommendation';
	import type { PageProps } from './$types';

	let { data }: PageProps = $props();

	let selectedCoffee = $state<CoffeeBeanResponse | undefined>(undefined);
	let detailsModalRef: ReturnType<typeof Modal>;
	function handleShowDetails(coffee: CoffeeBeanResponse) {
		selectedCoffee = coffee;
		detailsModalRef.open();
	}

	let ratingCoffeeId = $state<number | undefined>(undefined);

	function handleRatingModal(coffeeId: number) {
		ratingCoffeeId = coffeeId;
	}
</script>

<div class="w-200">
	<Card>
		<CoffeePagination
			purchases={data?.purchases}
			currentPage={data?.currentPage}
			totalPages={data?.totalPages}
			onShowDetails={handleShowDetails}
			onRatingClick={handleRatingModal}
		></CoffeePagination>
	</Card>
</div>

<Modal bind:this={detailsModalRef} headerTxt={selectedCoffee?.name}>
	{#if selectedCoffee}
		<DetailsModalContent details={selectedCoffee} />
	{/if}
</Modal>

<Modal headerTxt={m.rate_this_coffee()}>
	{#if ratingCoffeeId}
		{ratingCoffeeId} TEST
	{/if}
</Modal>
