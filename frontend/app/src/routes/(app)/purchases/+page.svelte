<script lang="ts">
	import { enhance } from '$app/forms';
	import CoffeePagination from '$lib/components/CoffeePagination.svelte';
	import Card from '$lib/components/core/Card.svelte';
	import Modal from '$lib/components/core/Modal.svelte';
	import SubmitButton from '$lib/components/core/SubmitButton.svelte';
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
	let ratingModalRef: ReturnType<typeof Modal>;
	function handleRatingModal(coffeeId: number) {
		ratingCoffeeId = coffeeId;
		ratingModalRef.open();
	}
	let rating = $state(0);
	let hoveredRating = $state(0);
</script>

<div class="w-210">
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

<Modal bind:this={ratingModalRef} headerTxt={m.rate_this_coffee()}>
	{#if ratingCoffeeId}
		<form
			id="rating-form"
			class="p-1"
			method="POST"
			use:enhance={() => {
				return async ({ result, update }) => {
					if (result.type === 'success') {
						ratingModalRef?.close();
					}
					await update();
				};
			}}
		>
			<input name="coffeeId" value={ratingCoffeeId} type="hidden" />
			<input name="rating" value={rating} type="hidden" />
			<div class="flex flex-row justify-center">
				{#each [1, 2, 3, 4, 5] as beanStar}
					<button
						type="button"
						class="px-0.5 transition-colors"
						onmouseenter={() => (hoveredRating = beanStar)}
						onmouseleave={() => {
							hoveredRating = 0;
						}}
						onclick={() => (rating = beanStar)}
					>
						{#if beanStar <= (hoveredRating || rating)}
							<span class="icon-[streamline--coffee-bean-solid] size-8 bg-attention"></span>
						{:else}
							<span class="icon-[streamline--coffee-bean] size-8 bg-attention"></span>
						{/if}
					</button>
				{/each}
			</div>
			<SubmitButton disabled={rating == 0}>{m.rate()}</SubmitButton>
		</form>
	{/if}
</Modal>
